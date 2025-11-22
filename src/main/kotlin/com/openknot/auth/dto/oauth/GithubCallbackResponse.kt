package com.openknot.auth.dto.oauth

data class GithubCallbackResponse(
    val success: Boolean,
    val message: String,
    val githubUsername: String? = null,
    val githubId: Long? = null,
)