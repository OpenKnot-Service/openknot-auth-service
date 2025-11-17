package com.openknot.auth.repository

import com.openknot.auth.config.TokenProperties
import com.openknot.auth.entity.RefreshToken
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRepository(
    private val tokenProperties: TokenProperties,
    private val coroutineRedisRepository: CoroutineRedisRepository,
) {
    suspend fun findByToken(token: String): RefreshToken? {
        val cacheKey = "refresh_token:$token"

        return coroutineRedisRepository.get(cacheKey, RefreshToken::class.java)
    }

    // TODO: 이중 저장 방식 개선 필요
    //  현재: user_refresh_token:xxx에 RefreshToken 객체 전체 저장 (메모리 낭비)
    //  개선: user_refresh_token:xxx에 token 값(String)만 저장
    //  수정 시 findByUserId()도 함께 수정 필요:
    //    1. user_refresh_token:xxx에서 token(String) 조회
    //    2. 조회된 token으로 findByToken() 호출
    suspend fun findByUserId(userId: String): RefreshToken? {
        val cacheKey = "user_refresh_token:$userId"

        return coroutineRedisRepository.get(cacheKey, RefreshToken::class.java)
    }

    // TODO: 이중 저장 방식 개선 필요
    //  현재 문제:
    //    1. 불필요한 RefreshToken 객체 재생성 (line 30-33)
    //    2. userIdCacheKey에 RefreshToken 객체 전체 저장 → token 값(String)만 저장해야 함
    //  수정 예시:
    //    coroutineRedisRepository.save(refreshTokenCacheKey, refreshToken, ttl)
    //    coroutineRedisRepository.save(userIdCacheKey, refreshToken.token, ttl)
    suspend fun saveToken(refreshToken: RefreshToken) {
        val refreshTokenCacheKey = "refresh_token:${refreshToken.token}"
        val userIdCacheKey = "user_refresh_token:${refreshToken.userId}"

        val ttl = Duration.ofMillis(tokenProperties.refreshTokenExpiration)
        val token = RefreshToken(
            userId = refreshToken.userId,
            token = refreshToken.token,
        )

        coroutineRedisRepository.save(refreshTokenCacheKey, token, ttl)
        coroutineRedisRepository.save(userIdCacheKey, token, ttl)
    }

    suspend fun deleteToken(key: String): Boolean {
        return coroutineRedisRepository.delete(key)
    }
}