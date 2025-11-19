package com.openknot.auth.service

import com.openknot.auth.feign.facade.UserFacade
import com.openknot.auth.repository.RefreshTokenRepository
import com.openknot.auth.support.RedisContainerSupport
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class AuthServiceTest(
    @Autowired private val authService: AuthService,
    @Autowired private val refreshTokenRepository: RefreshTokenRepository,
) : RedisContainerSupport() {

    @MockkBean
    lateinit var userFacade: UserFacade

    private val fixedUserId = UUID.randomUUID()

    @BeforeEach
    fun setupMocks() {
        coEvery { userFacade.getUserId("tester@example.com", "password123") } returns fixedUserId
    }

    @Test
    fun `로그인하면 Refresh Token이 Redis에 저장된다`() = runTest {
        val token = authService.login("tester@example.com", "password123")

        val saved = refreshTokenRepository.findByUserId(fixedUserId.toString())

        assertThat(saved).isNotNull
        assertThat(saved!!.token).isEqualTo(token.refreshToken)
    }

    @Test
    fun `Refresh 요청 시 기존 토큰이 삭제되고 새 토큰이 발급된다`() = runTest {
        val issued = authService.login("tester@example.com", "password123")

        val rotated = authService.refresh(issued.refreshToken)

        val latest = refreshTokenRepository.findByUserId(fixedUserId.toString())
        assertThat(latest!!.token).isEqualTo(rotated.refreshToken)
    }
}
