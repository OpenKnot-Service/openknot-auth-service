package com.openknot.auth.util

import com.openknot.auth.config.TokenProperties
import com.openknot.auth.dto.Token
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    private val tokenProperties: TokenProperties,
) {
    private val secretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenProperties.secret))
    }

    fun generateTokens(userId: UUID, role: String): Token {
        val now = Date()
        val accessToken = createAccessToken(userId, role, now)
        val refreshToken = createRefreshToken(userId, now)

        return Token(
            grantType = "Bearer",
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    // NOTE: Gateway의 로직과 동일함, 이후 수정할 경우 Gateway도 변경할 것.
    //          (Gateway Node의 API를 사용하려 하였으나, Gateway 트래픽 분산용으로 그냥 여기 만듬)
    fun getAuthentication(token: String): String? {
        val claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
        val userId = claims.subject

        return userId
    }

    fun extractUserIdFromHeader(authorizationHeader: String?): UUID {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        val token = authorizationHeader.substring(7)
        val userId = getAuthentication(token) ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

        return try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        }
    }

    private fun createAccessToken(userId: UUID, role: String, now: Date): String {
        val validity = Date(now.time + tokenProperties.accessTokenExpiration)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }

    private fun createRefreshToken(userId: UUID, now: Date): String {
        val validity = Date(now.time + tokenProperties.refreshTokenExpiration)
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }
}