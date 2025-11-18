package com.openknot.auth.dto

/**
 * API 응답에 노출되는 Access Token 컨테이너.
 * Refresh Token은 HttpOnly 쿠키로만 전송한다.
 */
data class AccessTokenResponse(
    val grantType: String,
    val accessToken: String,
)
