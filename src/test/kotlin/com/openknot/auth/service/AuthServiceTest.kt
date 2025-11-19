package com.openknot.auth.service

import com.openknot.auth.dto.Token
import com.openknot.auth.entity.RefreshToken
import com.openknot.auth.feign.facade.UserFacade
import com.openknot.auth.repository.RefreshTokenRepository
import com.openknot.auth.util.JwtProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
) {

    @MockkBean
    lateinit var userFacade: UserFacade

    @MockkBean
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    private val fixedUserId = UUID.randomUUID()

    @BeforeEach
    fun setupMocks() {
        coEvery { userFacade.getUserId("tester@example.com", "password123") } returns fixedUserId
    }

    @Test
    fun `로그인하면 Refresh Token이 저장된다`() = runTest {
        // Given
        val mockToken = Token(grantType = "Bearer", accessToken = "access-token", refreshToken = "refresh-token")
        coEvery { refreshTokenRepository.findByUserId(fixedUserId.toString()) } returns null
        coEvery { refreshTokenRepository.saveToken(any(), any()) } returns Unit
        every { jwtProvider.generateTokens(fixedUserId, "ROLE_USER") } returns mockToken

        // When
        val token = authService.login("tester@example.com", "password123")

        // Then
        assertThat(token.refreshToken).isEqualTo("refresh-token")
        coVerify { refreshTokenRepository.saveToken(fixedUserId.toString(), "refresh-token") }
    }

    @Test
    fun `Refresh 요청 시 기존 토큰이 삭제되고 새 토큰이 발급된다`() = runTest {
        // Given
        val oldToken = "old-refresh-token"
        val newToken = Token(grantType = "Bearer", accessToken = "new-access", refreshToken = "new-refresh")

        coEvery { refreshTokenRepository.findByToken(oldToken) } returns RefreshToken(fixedUserId.toString(), oldToken)
        every { jwtProvider.getAuthentication(oldToken) } returns fixedUserId.toString()
        coEvery { refreshTokenRepository.deleteToken(any()) } returns true
        every { jwtProvider.generateTokens(fixedUserId, "ROLE_USER") } returns newToken
        coEvery { refreshTokenRepository.saveToken(any(), any()) } returns Unit

        // When
        val rotated = authService.refresh(oldToken)

        // Then
        assertThat(rotated.refreshToken).isEqualTo("new-refresh")
        assertThat(rotated.refreshToken).isNotEqualTo(oldToken)
        coVerify(exactly = 2) { refreshTokenRepository.deleteToken(any()) }
        coVerify { refreshTokenRepository.saveToken(fixedUserId.toString(), "new-refresh") }
    }
}
