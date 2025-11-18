package com.openknot.auth.util

import com.openknot.auth.config.TokenProperties
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenCookieWriter(
    private val tokenProperties: TokenProperties,
) {

    fun write(
        response: ServerHttpResponse,
        refreshToken: String,
    ) {
        val cookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ofMillis(tokenProperties.refreshTokenExpiration))
            .path("/")
            .build()

        response.addCookie(cookie)
    }

    fun clear(response: ServerHttpResponse) {
        val expiredCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ZERO)
            .path("/")
            .build()

        response.addCookie(expiredCookie)
    }
}
