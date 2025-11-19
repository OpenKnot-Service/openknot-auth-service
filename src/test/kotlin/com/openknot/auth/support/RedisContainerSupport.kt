package com.openknot.auth.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

abstract class RedisContainerSupport {

    companion object {
        private val externalHost = System.getenv("IT_REDIS_HOST")
        private val externalPort = System.getenv("IT_REDIS_PORT")
        private val useExternalRedis = !externalHost.isNullOrBlank() && !externalPort.isNullOrBlank()

        private val redisContainer: GenericContainer<*>? =
            if (useExternalRedis) null
            else GenericContainer(DockerImageName.parse("redis:7.2")).withExposedPorts(6379)

        init {
            if (!useExternalRedis) {
                redisContainer!!.start()
                Runtime.getRuntime().addShutdownHook(Thread { redisContainer.stop() })
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerRedisProperties(registry: DynamicPropertyRegistry) {
            if (useExternalRedis) {
                registry.add("spring.data.redis.host") { externalHost }
                registry.add("spring.data.redis.port") { externalPort!!.toInt() }
            } else {
                registry.add("spring.data.redis.host") { redisContainer!!.host }
                registry.add("spring.data.redis.port") { redisContainer!!.getMappedPort(6379) }
            }
        }
    }
}
