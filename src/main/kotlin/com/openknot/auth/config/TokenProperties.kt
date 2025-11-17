package com.openknot.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class TokenProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long
)