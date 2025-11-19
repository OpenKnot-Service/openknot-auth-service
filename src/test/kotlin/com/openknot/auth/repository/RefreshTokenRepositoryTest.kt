package com.openknot.auth.repository

import com.openknot.auth.support.RedisContainerSupport
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class RefreshTokenRepositoryTest(
    @Autowired private val refreshTokenRepository: RefreshTokenRepository,
) : RedisContainerSupport() {

    @Test
    fun `Refresh Token을 저장하고 토큰으로 조회할 수 있다`() = runTest {
        val userId = UUID.randomUUID().toString()
        val refreshToken = "refresh-${UUID.randomUUID()}"

        refreshTokenRepository.saveToken(userId, refreshToken)

        val saved = refreshTokenRepository.findByToken(refreshToken)

        assertThat(saved).isNotNull
        assertThat(saved!!.userId).isEqualTo(userId)
        assertThat(saved.token).isEqualTo(refreshToken)
    }

    @Test
    fun `userId로 조회하면 최신 Refresh Token을 돌려준다`() = runTest {
        val userId = UUID.randomUUID().toString()
        val firstToken = "refresh-${UUID.randomUUID()}"
        val secondToken = "refresh-${UUID.randomUUID()}"

        refreshTokenRepository.saveToken(userId, firstToken)
        refreshTokenRepository.saveToken(userId, secondToken)

        val found = refreshTokenRepository.findByUserId(userId)

        assertThat(found).isNotNull
        assertThat(found!!.token).isEqualTo(secondToken)
    }
}
