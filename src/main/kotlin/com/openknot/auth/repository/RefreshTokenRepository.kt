package com.openknot.auth.repository

import com.openknot.auth.entity.RefreshToken
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRepository(
    private val coroutineRedisRepository: CoroutineRedisRepository,
) {
    suspend fun findByToken(token: String): RefreshToken? {
        val cacheKey = "refresh_token:$token"

        return coroutineRedisRepository.get(cacheKey, RefreshToken::class.java)
    }

    suspend fun saveToken(refreshToken: RefreshToken) {
        val cacheKey = "refresh_token:${refreshToken.token}"

        coroutineRedisRepository.save(
            key = cacheKey,
            value = refreshToken.userId,
            // TODO: jwt property로 설정하도록 변경
            ttl = Duration.ofHours(2)
        )
    }
}