package com.openknot.auth.service

import com.openknot.auth.dto.Token
import com.openknot.auth.entity.RefreshToken
import com.openknot.auth.util.JwtProvider
import com.openknot.auth.feign.facade.UserFacade
import com.openknot.auth.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userFacade: UserFacade,
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    suspend fun login(id: String, password: String): Token {
        val userId = userFacade.getUserId(id, password)


        // 1. 리프레시 토큰이 이미 있을 경우 삭제 후 새로운 토큰 저장
        refreshTokenRepository.findByUserId(userId.toString())?.let {
            refreshTokenRepository.deleteToken("user_refresh_token:${it.userId}")
            refreshTokenRepository.deleteToken("refresh_token:${it.token}")
        }

        val createdToken = jwtProvider.generateTokens(userId, "ROLE_USER")

        refreshTokenRepository.saveToken(RefreshToken(
            userId = userId.toString(),
            token = createdToken.refreshToken,
        ))

        return createdToken
    }
}