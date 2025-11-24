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

    @GetMapping("/github/callback", produces = ["text/html"])
    suspend fun githubCallback(
        @RequestParam("code") code: String,
        @RequestParam("state") state: String,
    ): ResponseEntity<String> {
        return try {
            val response = githubOAuthService.processGithubCallback(code, state)
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>GitHub 연동 완료</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                            background: #f5f5f5;
                        }
                        .message {
                            text-align: center;
                            padding: 2rem;
                            background: white;
                            border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        }
                        .success { color: #22c55e; }
                    </style>
                </head>
                <body>
                    <div class="message">
                        <h2 class="success">✓ GitHub 연동 완료</h2>
                        <p>${response.message}</p>
                        <p style="color: #666; font-size: 14px;">잠시 후 창이 자동으로 닫힙니다...</p>
                    </div>
                    <script>
                        setTimeout(() => window.close(), 1500);
                    </script>
                </body>
                </html>
            """.trimIndent()
            ResponseEntity.ok(html)
        } catch (e: BusinessException) {
            logger.error(e) { "GitHub OAuth callback failed: ${e.message}" }
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>GitHub 연동 실패</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                            background: #f5f5f5;
                        }
                        .message {
                            text-align: center;
                            padding: 2rem;
                            background: white;
                            border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        }
                        .error { color: #ef4444; }
                    </style>
                </head>
                <body>
                    <div class="message">
                        <h2 class="error">✗ GitHub 연동 실패</h2>
                        <p>${e.errorCode.code}</p>
                        <p style="color: #666; font-size: 14px;">잠시 후 창이 자동으로 닫힙니다...</p>
                    </div>
                    <script>
                        setTimeout(() => window.close(), 2000);
                    </script>
                </body>
                </html>
            """.trimIndent()
            ResponseEntity.status(e.errorCode.status).body(html)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during GitHub OAuth callback" }
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>GitHub 연동 실패</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                            background: #f5f5f5;
                        }
                        .message {
                            text-align: center;
                            padding: 2rem;
                            background: white;
                            border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        }
                        .error { color: #ef4444; }
                    </style>
                </head>
                <body>
                    <div class="message">
                        <h2 class="error">✗ GitHub 연동 실패</h2>
                        <p>GitHub 계정 연동 중 오류가 발생했습니다.</p>
                        <p style="color: #666; font-size: 14px;">잠시 후 창이 자동으로 닫힙니다...</p>
                    </div>
                    <script>
                        setTimeout(() => window.close(), 2000);
                    </script>
                </body>
                </html>
            """.trimIndent()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(html)
        }
    }
}
