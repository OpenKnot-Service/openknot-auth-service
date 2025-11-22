package com.openknot.auth.dto.oauth

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubTokensResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,
    @field:JsonProperty("token_type")
    val tokenType: String,
    val scope: String,
)