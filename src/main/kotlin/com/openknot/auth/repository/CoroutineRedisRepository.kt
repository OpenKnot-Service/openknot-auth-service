package com.openknot.auth.repository

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CoroutineRedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) {
    private val valueOps = redisTemplate.opsForValue()

    suspend fun save(key: String, value: Any, ttl: Duration) {
        valueOps.set(key, value, ttl).awaitSingleOrNull()
    }

    suspend fun <T> get(key: String, type: Class<T>): T? {
        val result = valueOps.get(key).awaitSingleOrNull() ?: return null
        return when {
            type.isInstance(result) -> type.cast(result)
            result is Map<*, *> -> objectMapper.convertValue(result, type)
            else -> null
        }
    }

    suspend fun delete(key: String): Boolean {
        return valueOps.delete(key).awaitSingleOrNull() ?: false
    }
}
