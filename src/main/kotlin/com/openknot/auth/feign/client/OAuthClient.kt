package com.openknot.auth.feign.client

import com.openknot.auth.dto.ErrorResponse
import com.openknot.auth.dto.oauth.GithubLinkRequest
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import com.openknot.auth.exception.UserServiceException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

@Component
class OAuthClient(
    @param:Qualifier("userServiceWebClient") private val webClient: WebClient
) {
    private val logger = KotlinLogging.logger {}

    suspend fun linkGithubAccount(
        request: GithubLinkRequest,
    ) {
        try {
            webClient.post()
                .uri("/github/link")
                .header("X-User-Id", request.userId.toString())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError) { response ->
                    propagateUserServiceError(response)
                }
                .awaitBody<Unit>()
        } catch (e: UserServiceException) {
            throw e
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error linking GitHub account for user: ${request.userId}" }
            throw BusinessException(ErrorCode.INVALID_ERROR_CODE)
        }
    }

    private fun propagateUserServiceError(response: ClientResponse): Mono<Throwable> {
        return response.bodyToMono(ErrorResponse::class.java)
            .switchIfEmpty(
                Mono.just(
                    ErrorResponse(
                        code = ErrorCode.INVALID_ERROR_CODE.code,
                        message = "User Service error without body.",
                    )
                )
            )
            .flatMap { errorBody ->
                logger.error {
                    "User Service error linking GitHub account: status=${response.statusCode().value()}, code=${errorBody.code}"
                }
                Mono.error(UserServiceException(response.statusCode(), errorBody))
            }
    }
}
