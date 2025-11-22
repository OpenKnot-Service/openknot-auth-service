package com.openknot.auth.exception

import com.openknot.auth.dto.ErrorResponse
import org.springframework.http.HttpStatusCode

class UserServiceException(
    val statusCode: HttpStatusCode,
    val errorResponse: ErrorResponse,
) : RuntimeException("User service error: ${errorResponse.code}")
