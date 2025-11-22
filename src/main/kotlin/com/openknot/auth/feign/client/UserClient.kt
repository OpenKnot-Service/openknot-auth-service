package com.openknot.auth.feign.client

import com.openknot.auth.dto.CredentialValidationRequest
import com.openknot.auth.dto.RegisterRequest
import com.openknot.auth.dto.UserIdResponse
import com.openknot.auth.dto.UserInfoResponse
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

@Component
class UserClient(
    @param:Qualifier("userServiceWebClient") private val webClient: WebClient
) {
    private val logger = KotlinLogging.logger {}

    suspend fun validateCredentials(email: String, password: String): UserIdResponse {
        return try {
            webClient.post()
                .uri("/validate-credentials")
                .bodyValue(CredentialValidationRequest(email, password))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { response ->
                    handleClientError(response.statusCode())
                }
                .onStatus(HttpStatusCode::is5xxServerError) { response ->
                    handleServerError(response.statusCode())
                }
                .awaitBody<UserIdResponse>()
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error calling user service for email: $email" }
            throw BusinessException(ErrorCode.INVALID_ERROR_CODE)
        }
    }

    suspend fun existsUserEmail(
        email: String,
    ): Boolean {
        return try {
            webClient.get()
                .uri("/email-exists?email={email}", email)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { response ->
                    handleClientError(response.statusCode())
                }
                .onStatus(HttpStatusCode::is5xxServerError) { response ->
                    handleServerError(response.statusCode())
                }
                .awaitBody<Boolean>()
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error calling user service for email: $email" }
            throw BusinessException(ErrorCode.INVALID_ERROR_CODE)
        }
    }

    suspend fun createUser(
        request: RegisterRequest,
    ): UserInfoResponse {
        return try {
            webClient.post()
                .uri("/create")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { response ->
                    handleClientError(response.statusCode())
                }
                .onStatus(HttpStatusCode::is5xxServerError) { response ->
                    handleServerError(response.statusCode())
                }
                .awaitBody<UserInfoResponse>()
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error calling user service for email: ${request.email}" }
            throw BusinessException(ErrorCode.INVALID_ERROR_CODE)
        }
    }

    private fun handleClientError(statusCode: HttpStatusCode): Mono<Throwable> {
        return when (statusCode.value()) {
            404 -> Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND))
            401 -> Mono.error(BusinessException(ErrorCode.INVALID_PASSWORD))
            else -> {
                logger.error { "Client error from user service: ${statusCode.value()}" }
                Mono.error(BusinessException(ErrorCode.INVALID_ERROR_CODE))
            }
        }
    }

    private fun handleServerError(statusCode: HttpStatusCode): Mono<Throwable> {
        logger.error { "Server error from user service: ${statusCode.value()}" }
        return Mono.error(BusinessException(ErrorCode.INVALID_ERROR_CODE))
    }
}