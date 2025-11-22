package com.openknot.auth.dto.oauth

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubAccessTokenRequest(
    @field:JsonProperty("client_id")
    val clientId: String,
    @field:JsonProperty("client_secret")
    val clientSecret: String,
    val code: String,
)