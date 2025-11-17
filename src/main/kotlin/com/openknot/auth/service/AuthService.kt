package com.openknot.auth.service

import com.openknot.auth.dto.Token
import com.openknot.auth.util.JwtProvider
import com.openknot.auth.feign.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userFacade: UserFacade,
    private val jwtProvider: JwtProvider
) {
    @Transactional(readOnly = true)
    suspend fun login(id: String, password: String): Token {
        val userId = userFacade.getUserId(id, password)

        return jwtProvider.generateTokens(userId, "ROLE_USER")
    }
}