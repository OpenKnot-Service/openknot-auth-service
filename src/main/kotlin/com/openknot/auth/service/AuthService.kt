package com.openknot.auth.service

import com.openknot.auth.dto.Token
import com.openknot.auth.entity.RefreshToken
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import com.openknot.auth.feign.facade.UserFacade
import com.openknot.auth.repository.RefreshTokenRepository
import com.openknot.auth.util.JwtProvider
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userFacade: UserFacade,
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    suspend fun login(id: String, password: String): Token {
        val userId = userFacade.getUserId(id, password)

        // 1. 리프레시 토큰이 이미 있을 경우 삭제 후 새로운 토큰 저장
        refreshTokenRepository.findByUserId(userId.toString())?.let { deleteToken(it) }

        val createdToken = jwtProvider.generateTokens(userId, "ROLE_USER")

        refreshTokenRepository.saveToken(
            userId = userId.toString(),
            refreshToken = createdToken.refreshToken,
        )

        return createdToken
    }

    suspend fun refresh(
        token: String,
    ): Token {
        // 1. 토큰 검증
        val refreshToken = refreshTokenRepository.findByToken(token)
            ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        val userId = jwtProvider.getAuthentication(refreshToken.token)
            ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        if (userId != refreshToken.userId) throw BusinessException(ErrorCode.TOKEN_INVALID)

        // 2. Redis에서 Refresh Token 삭제
        deleteToken(refreshToken)

        // 3. 토큰 재발급
        val generatedToken = jwtProvider.generateTokens(
            userId = UUID.fromString(userId),
            role = "ROLE_USER"
        )

        // 4. Redis에 등록
        refreshTokenRepository.saveToken(
            userId = refreshToken.userId,
            refreshToken = generatedToken.refreshToken,
        )

        // 5. 반환
        return generatedToken
    }

    suspend fun logout(refreshToken: String) {
        val token = refreshTokenRepository.findByToken(refreshToken)
            ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        val userId = jwtProvider.getAuthentication(token.token)
            ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        if (userId != token.userId) throw BusinessException(ErrorCode.TOKEN_INVALID)

        deleteToken(token)
    }

    private suspend fun deleteToken(token: RefreshToken) {
        refreshTokenRepository.deleteToken("refresh_token:${token.token}")
        refreshTokenRepository.deleteToken("user_refresh_token:${token.userId}")
    }
}
