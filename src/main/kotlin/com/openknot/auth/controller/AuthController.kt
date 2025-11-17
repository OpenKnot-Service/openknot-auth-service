package com.openknot.auth.controller

import com.openknot.auth.dto.LoginRequest
import com.openknot.auth.dto.Token
import com.openknot.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/login")
    suspend fun login(
        @RequestBody @Valid request: LoginRequest,
    ): ResponseEntity<Token> {
        return ResponseEntity.ok(
            authService.login(
                id = request.email,
                password = request.password
            )
        )
    }
}