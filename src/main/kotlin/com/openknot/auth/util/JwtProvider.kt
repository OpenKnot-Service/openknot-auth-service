package com.openknot.auth.util

import com.openknot.auth.config.TokenProperties
import com.openknot.auth.dto.Token
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