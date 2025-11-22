package com.openknot.auth.controller

import com.openknot.auth.dto.AccessTokenResponse
import com.openknot.auth.dto.LoginRequest
import com.openknot.auth.dto.RegisterRequest
import com.openknot.auth.service.AuthService
import com.openknot.auth.util.RefreshTokenCookieWriter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService,
    private val refreshTokenCookieWriter: RefreshTokenCookieWriter,
) {

    @PostMapping("/login")
    suspend fun login(
        @RequestBody @Valid request: LoginRequest,
        response: ServerHttpResponse,
    ): ResponseEntity<AccessTokenResponse> {
        val generatedToken = authService.login(
            id = request.email,
            password = request.password
        )

        refreshTokenCookieWriter.write(response, generatedToken.refreshToken)
        return ResponseEntity.ok(
            AccessTokenResponse(
                grantType = generatedToken.grantType,
                accessToken = generatedToken.accessToken,
            )
        )
    }

    @PostMapping("/register")
    suspend fun register(
        @RequestBody @Valid request: RegisterRequest,
        response: ServerHttpResponse,
    ): ResponseEntity<Unit> {
        authService.register(request)
        return ResponseEntity.status(201).build()
    }

    @PostMapping("/refresh")
    suspend fun refresh(
        @CookieValue("refreshToken", required = true) token: String,
        response: ServerHttpResponse,
    ): ResponseEntity<AccessTokenResponse> {
        val refreshedToken = authService.refresh(token)

        refreshTokenCookieWriter.write(response, refreshedToken.refreshToken)
        return ResponseEntity.ok(
            AccessTokenResponse(
                grantType = refreshedToken.grantType,
                accessToken = refreshedToken.accessToken,
            )
        )
    }

    @PostMapping("/logout")
    suspend fun logout(
        @CookieValue("refreshToken", required = true) refreshToken: String,
        response: ServerHttpResponse,
    ): ResponseEntity<Unit> {
        authService.logout(refreshToken)
        refreshTokenCookieWriter.clear(response)
        return ResponseEntity.noContent().build()
    }
}
