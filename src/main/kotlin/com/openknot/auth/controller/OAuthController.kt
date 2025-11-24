package com.openknot.auth.controller

import com.openknot.auth.dto.oauth.GithubCallbackResponse
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import com.openknot.auth.service.GithubOAuthService
import com.openknot.auth.util.JwtProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

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

    @GetMapping("/github/callback")
    suspend fun githubCallback(
        @RequestParam("code") code: String,
        @RequestParam("state") state: String,
    ): ResponseEntity<GithubCallbackResponse> {
        return try {
            val response = githubOAuthService.processGithubCallback(code, state)
            ResponseEntity.ok(response)
        } catch (e: BusinessException) {
            logger.error(e) { "GitHub OAuth callback failed: ${e.message}" }
            ResponseEntity.status(e.errorCode.status).body(
                GithubCallbackResponse(
                    success = false,
                    message = e.errorCode.code,
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during GitHub OAuth callback" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GithubCallbackResponse(
                    success = false,
                    message = "GitHub 계정 연동 중 오류가 발생했습니다.",
                )
            )
        }
    }
}
