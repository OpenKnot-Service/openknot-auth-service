package com.openknot.auth.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

abstract class RedisContainerSupport {

    companion object {
        private val redisContainer =
            GenericContainer(DockerImageName.parse("redis:7.2")).withExposedPorts(6379)

        init {
            redisContainer.start()
            Runtime.getRuntime().addShutdownHook(Thread { redisContainer.stop() })
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerRedisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }
}
