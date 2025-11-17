package com.openknot.auth.entity

import java.util.UUID

data class RefreshToken(
    val userId: UUID,
    val token: String,
    val expiryTime: Long,
)