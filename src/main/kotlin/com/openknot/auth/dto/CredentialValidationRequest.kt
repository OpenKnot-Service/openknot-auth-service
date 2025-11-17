package com.openknot.auth.dto

data class CredentialValidationRequest(
    val email: String,
    val password: String
)
