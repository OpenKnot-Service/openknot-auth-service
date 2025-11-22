package com.openknot.auth.dto.oauth

import java.util.UUID

data class GithubLinkRequest(
    val userId: UUID,
    val githubId: Long,
    val githubUsername: String,
    val githubAccessToken: String,
    val avatarUrl: String?,
)
