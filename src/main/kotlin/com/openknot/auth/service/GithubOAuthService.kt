package com.openknot.auth.service

import com.openknot.auth.config.OAuthProperties
import com.openknot.auth.dto.oauth.GithubAccessTokenRequest
import com.openknot.auth.dto.oauth.GithubCallbackResponse
import com.openknot.auth.dto.oauth.GithubLinkRequest
import com.openknot.auth.dto.oauth.GithubTokensResponse
import com.openknot.auth.dto.oauth.GithubUserResponse
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import com.openknot.auth.feign.facade.OAuthFacade
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class GithubOAuthService(
    private val oAuthProperties: OAuthProperties,
    @field:Qualifier("githubOAuthWebClient") private val githubOAuthWebClient: WebClient,
    @field:Qualifier("githubApiWebClient") private val githubApiWebClient: WebClient,
    private val redisTemplate: RedisTemplate<String, String>,
    private val oAuthFacade: OAuthFacade,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val STATE_KEY_PREFIX = "github_oauth:"
        private const val STATE_EXPIRATION_MINUTES = 10L
    }

    fun getAuthorizationUrl(state: String): String {
        // GitHub OAuth scope는 공백으로 구분되어야 함
        // 설정 파일의 쉼표와 띄어쓰기를 공백 하나로 정규화 (예: "read:user,admin:org" -> "read:user admin:org")
        val normalizedScope = oAuthProperties.scope
            .replace(",", " ")  // 쉼표를 공백으로 변환
            .replace(Regex("\\s+"), " ")  // 여러 개의 공백을 하나로
            .trim()

        return UriComponentsBuilder
            .fromHttpUrl("https://github.com/login/oauth/authorize")
            .queryParam("client_id", oAuthProperties.clientId)
            .queryParam("redirect_uri", oAuthProperties.redirectUri)
            .queryParam("scope", normalizedScope)  // scope=read%3Auser%20admin%3Aorg 형식으로 인코딩
            .queryParam("state", state)
            .encode()  // URI 인코딩 수행
            .build()
            .toUriString()
    }

    suspend fun exchangeCodeForToken(code: String): String {
        return try {
            val request = GithubAccessTokenRequest(
                clientId = oAuthProperties.clientId,
                clientSecret = oAuthProperties.clientSecret,
                code = code,
            )

            val response = githubOAuthWebClient.post()
                .uri("/access_token")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { response ->
                    logger.error { "GitHub token exchange failed with status: ${response.statusCode()}" }
                    Mono.error(BusinessException(ErrorCode.GITHUB_TOKEN_EXCHANGE_FAILED))
                }
                .onStatus(HttpStatusCode::is5xxServerError) { response ->
                    logger.error { "GitHub server error: ${response.statusCode()}" }
                    Mono.error(BusinessException(ErrorCode.GITHUB_API_ERROR))
                }
                .awaitBody<GithubTokensResponse>()

            response.accessToken
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during GitHub token exchange" }
            throw BusinessException(ErrorCode.GITHUB_API_ERROR)
        }
    }

    suspend fun getGithubUser(accessToken: String): GithubUserResponse {
        return try {
            githubApiWebClient.get()
                .uri("/user")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { response ->
                    logger.error { "GitHub user fetch failed with status: ${response.statusCode()}" }
                    Mono.error(BusinessException(ErrorCode.GITHUB_API_ERROR))
                }
                .onStatus(HttpStatusCode::is5xxServerError) { response ->
                    logger.error { "GitHub server error: ${response.statusCode()}" }
                    Mono.error(BusinessException(ErrorCode.GITHUB_API_ERROR))
                }
                .awaitBody<GithubUserResponse>()
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during GitHub user fetch" }
            throw BusinessException(ErrorCode.GITHUB_API_ERROR)
        }
    }

    fun processGithubLogin(userId: UUID): String {
        // 1. state 생성
        val state = generateState()

        // 2. Redis에 저장
        saveState(state, userId)

        // 3. GitHub OAuth URL 생성
        val githubAuthUrl = getAuthorizationUrl(state)

        logger.debug { "GitHub OAuth login initiated for user: $userId" }
        return githubAuthUrl
    }

    suspend fun processGithubCallback(code: String, state: String): GithubCallbackResponse {
        return try {
            // 1. state 검증 및 userId 가져오기
            val userId = getAndDeleteState(state)

            // 2. GitHub Access Token 획득
            val githubAccessToken = exchangeCodeForToken(code)

            // 3. GitHub 사용자 정보 조회
            val githubUser = getGithubUser(githubAccessToken)

            // 4. User 서비스에 GitHub 계정 연동 저장
            val linkRequest = GithubLinkRequest(
                userId = userId,
                githubId = githubUser.id,
                githubUsername = githubUser.login,
                githubAccessToken = githubAccessToken,
                avatarUrl = githubUser.avatarUrl,
            )
            oAuthFacade.linkGithubAccount(linkRequest)

            logger.info { "GitHub account linked successfully for user: $userId, github: ${githubUser.login}" }

            // 5. 성공 응답
            GithubCallbackResponse(
                success = true,
                message = "GitHub 계정 연동이 완료되었습니다.",
                githubUsername = githubUser.login,
                githubId = githubUser.id,
            )
        } catch (e: com.openknot.auth.exception.UserServiceException) {
            // User Service 에러를 그대로 전파 (OAuthController에서 처리)
            logger.error(e) { "User Service error during GitHub OAuth callback: ${e.errorResponse.message}" }
            throw e
        } catch (e: BusinessException) {
            logger.error(e) { "GitHub OAuth callback failed: ${e.message}" }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during GitHub OAuth callback" }
            throw BusinessException(ErrorCode.GITHUB_API_ERROR)
        }
    }

    fun generateState(): String {
        return UUID.randomUUID().toString()
    }

    fun saveState(state: String, userId: UUID) {
        redisTemplate.opsForValue().set(
            "$STATE_KEY_PREFIX$state",
            userId.toString(),
            STATE_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        )
        logger.debug { "State saved to Redis: $state for user: $userId" }
    }

    fun getAndDeleteState(state: String): UUID {
        val key = "$STATE_KEY_PREFIX$state"
        val userId = redisTemplate.opsForValue().get(key)
            ?: throw BusinessException(ErrorCode.GITHUB_STATE_MISMATCH)

        // state 삭제 (한 번만 사용)
        redisTemplate.delete(key)
        logger.debug { "State validated and deleted: $state" }

        return try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid UUID in Redis for state: $state" }
            throw BusinessException(ErrorCode.GITHUB_STATE_MISMATCH)
        }
    }
}
