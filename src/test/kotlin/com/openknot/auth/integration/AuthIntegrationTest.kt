package com.openknot.auth.integration

import com.openknot.auth.dto.Token
import com.openknot.auth.feign.facade.UserFacade
import com.openknot.auth.repository.RefreshTokenRepository
import com.openknot.auth.util.JwtProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var userFacade: UserFacade

    @MockkBean
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @Test
    fun `로그인 성공 시 Access Token과 Refresh Token을 발급한다`() {
        // Given
        val userId = UUID.randomUUID()
        val mockToken = Token(grantType = "Bearer", accessToken = "test-access-token", refreshToken = "test-refresh-token")

        coEvery { userFacade.getUserId("test@example.com", "password123") } returns userId
        coEvery { refreshTokenRepository.findByUserId(userId.toString()) } returns null
        coEvery { refreshTokenRepository.saveToken(any(), any()) } returns Unit
        every { jwtProvider.generateTokens(userId, "ROLE_USER") } returns mockToken

        // When & Then
        val response = webTestClient.post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.accessToken").isEqualTo("test-access-token")
            .returnResult()

        val refreshCookie = response.responseCookies["refreshToken"]
        assertThat(refreshCookie).isNotNull
        assertThat(refreshCookie!!).isNotEmpty
        assertThat(refreshCookie.first().value).isEqualTo("test-refresh-token")
    }

    @Test
    fun `유효하지 않은 이메일 포맷이면 400을 반환한다`() {
        webTestClient.post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isBadRequest
    }
}
