package com.openknot.auth.feign.facade

import com.openknot.auth.feign.client.UserClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserFacade(
    private val userClient: UserClient,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun getUserId(email: String, password: String): UUID {
        val response = userClient.validateCredentials(email, password)
        return response.userId
    }
}