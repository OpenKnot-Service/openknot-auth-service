package com.openknot.auth.dto

import java.time.LocalDateTime

data class UserInfoResponse(
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val description: String? = null,
    val githubLink: String? = null,
    val createdAt: LocalDateTime,
)
