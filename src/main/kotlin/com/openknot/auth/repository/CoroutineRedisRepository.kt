package com.openknot.auth.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CoroutineRedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) {
    private val valueOps = redisTemplate.opsForValue()

    suspend fun save(key: String, value: Any, ttl: Duration) {
        // set()은 Mono<Boolean>을 반환 -> awaitOrNull()로 suspend 함수로 변환
        valueOps.set(key, value, ttl).awaitSingleOrNull()
    }

    suspend fun <T> get(key: String, type: Class<T>): T? {
        // get()은 Mono<Any>를 반환 -> awaitOrNull()로 변환
        val result = valueOps.get(key).awaitSingleOrNull()
        // 원하는 타입으로 안전하게 캐스팅
        return if (type.isInstance(result)) type.cast(result) else null
    }

    suspend fun delete(key: String): Boolean {
        // delete()는 Mono<Boolean>을 반환 -> awaitOrNull()로 변환 후 null이면 false 처리
        return valueOps.delete(key).awaitSingleOrNull() ?: false
    }
}