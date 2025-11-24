package com.openknot.auth.controller

import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.UserServiceException
import com.openknot.auth.service.GithubOAuthService
import com.openknot.auth.util.JwtProvider
import com.openknot.auth.view.GithubOAuthCallbackPage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class OAuthController(
    private val githubOAuthService: GithubOAuthService,
    private val jwtProvider: JwtProvider,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/github")
    fun githubLogin(
        @RequestHeader("Authorization") authorization: String,
    ): Mono<Map<String, String>> {
        val userId = jwtProvider.extractUserIdFromHeader(authorization)
        val githubAuthUrl = githubOAuthService.processGithubLogin(userId)

        // JSON 응답으로 URL 반환 (fetch가 리다이렉트를 따라가지 않도록)
        return Mono.just(mapOf("url" to githubAuthUrl))
    }

    @GetMapping("/github/callback", produces = ["text/html"])
    suspend fun githubCallback(
        @RequestParam("code") code: String,
        @RequestParam("state") state: String,
    ): ResponseEntity<String> {
        return try {
            val response = githubOAuthService.processGithubCallback(code, state)
            ResponseEntity.ok(GithubOAuthCallbackPage.success(response.message))
        } catch (e: UserServiceException) {
            // User Service에서 반환한 에러 메시지를 그대로 사용
            logger.error(e) { "User Service error during GitHub OAuth callback: ${e.errorResponse.message}" }
            ResponseEntity.status(e.statusCode)
                .body(GithubOAuthCallbackPage.error(e.errorResponse.message ?: "사용자 서비스 오류가 발생했습니다."))
        } catch (e: BusinessException) {
            logger.error(e) { "GitHub OAuth callback failed: ${e.message}" }
            ResponseEntity.status(e.errorCode.status)
                .body(GithubOAuthCallbackPage.error(e.errorCode.code))
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during GitHub OAuth callback" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GithubOAuthCallbackPage.error("GitHub 계정 연동 중 오류가 발생했습니다."))
        }
    }
}
