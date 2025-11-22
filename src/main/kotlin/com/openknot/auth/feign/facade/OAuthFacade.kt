package com.openknot.auth.feign.facade

import com.openknot.auth.dto.oauth.GithubLinkRequest
import com.openknot.auth.feign.client.OAuthClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OAuthFacade(
    private val oAuthClient: OAuthClient,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun linkGithubAccount(request: GithubLinkRequest) {
        oAuthClient.linkGithubAccount(request)
    }
}
