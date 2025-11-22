package com.openknot.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.github")
data class OAuthProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scope: String,
)