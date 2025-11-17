package com.openknot.auth.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
) {
    // User Error
    ALREADY_EMAIL(HttpStatus.CONFLICT, "USER.001"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER.002"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER.003"),
//    UNSUPPORTED_AUTH_PROVIDER(HttpStatus.BAD_REQUEST, "USER.003"),

    // System Error
    INVALID_ERROR_CODE(HttpStatus.BAD_REQUEST, "SYSTEM.001"),

    // Validation
    DEFAULT_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALID.001"),
    DEFAULT_NOT_NULL_MESSAGE(HttpStatus.BAD_REQUEST, "VALID.002"),
    DEFAULT_NOT_BLANK_MESSAGE(HttpStatus.BAD_REQUEST, "VALID.003"),
    DEFAULT_SIZE_MESSAGE(HttpStatus.BAD_REQUEST, "VALID.004"),
    DEFAULT_MIN_MESSAGE(HttpStatus.BAD_REQUEST, "VALID.005"),
    DEFAULT_MAX_MESSAGE(HttpStatus.BAD_REQUEST, "VALID.006"),
    DEFAULT_RANGE_MESSAGE(HttpStatus.BAD_REQUEST, "VALID.007"),

    // For test
    FOO(HttpStatus.INTERNAL_SERVER_ERROR, "FOO.001"),
}