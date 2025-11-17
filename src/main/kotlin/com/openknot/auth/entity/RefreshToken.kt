package com.openknot.auth.entity

data class RefreshToken(
    val userId: String,
    val token: String,
)