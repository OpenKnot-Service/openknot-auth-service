package com.openknot.auth.dto.oauth

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubUserResponse(
    val id: Long,
    val login: String,
    val name: String?,
    val email: String?,
    @field:JsonProperty("avatar_url")
    val avatarUrl: String?,
    val bio: String?,
    @field:JsonProperty("public_repos")
    val publicRepos: Int,
)
