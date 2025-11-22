package com.openknot.auth.service

import com.openknot.auth.dto.RegisterRequest
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

    suspend fun register(
        request: RegisterRequest,
    ): Boolean {
        // 1. 이메일 중복 확인
        if (userFacade.existsUserEmail(request.email)) throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        // 2. 비밀번호 강도 검증
        validatePassword(request.password)
        // 3. 이름 검증
        validateName(request.name)

        // 4. URL 검증
        request.profileImageUrl?.let { validateUrl(it) }
        request.githubLink?.let { validateUrl(it) }

        // 5. 설명 검증
        request.description?.let { validateDescription(it) }

        // 6. 계정 생성
        userFacade.createUser(request)

        return true
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT)
        }

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { it in "@\$!%*?&" }

        if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecialChar) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT)
        }
    }

    private fun validateName(name: String) {
        if (name.length !in 2..50) {
            throw BusinessException(ErrorCode.INVALID_NAME_LENGTH)
        }
    }

    private fun validateUrl(url: String) {
        val urlPattern = "^https?://.+".toRegex()
        if (!url.matches(urlPattern)) {
            throw BusinessException(ErrorCode.INVALID_URL_FORMAT)
        }
    }

    private fun validateDescription(description: String?) {
        description?.let {
            if (description.length > 500) {
                throw BusinessException(ErrorCode.INVALID_DESCRIPTION_LENGTH)
            }
        }
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
