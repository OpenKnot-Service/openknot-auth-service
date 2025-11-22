package com.openknot.auth.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
) {
    // Auth Error
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인 정보가 만료되었습니다."),
    // 그외 모든 JWT 관련 예외
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 로그인 정보입니다."),
    // 토큰 서명 오류
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "로그인 정보의 서명이 유효하지 않습니다."),
    // 토큰 형식이 잘못되었을때
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "로그인 정보의 서명이 유효하지 않습니다."),
    // 지원되지 않는 형식의 토큰
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원되지 않는 로그인 정보입니다."),
    // 토큰이 존재하지 않음
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "로그인 정보가 존재하지 않습니다."),

    // User Error
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER.002"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER.003"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER.001"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
//    UNSUPPORTED_AUTH_PROVIDER(HttpStatus.BAD_REQUEST, "USER.003"),

    // GitHub OAuth Error
    GITHUB_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "GitHub 인증 코드로 액세스 토큰을 받아오는데 실패했습니다."),
    GITHUB_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GitHub API 호출 중 오류가 발생했습니다."),
    GITHUB_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "GitHub OAuth state 값이 일치하지 않습니다."),

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
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이며, 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."),
    INVALID_NAME_LENGTH(HttpStatus.BAD_REQUEST, "이름은 2자 이상 50자 이하여야 합니다."),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "올바른 URL 형식이 아닙니다."),
    INVALID_DESCRIPTION_LENGTH(HttpStatus.BAD_REQUEST, "소개는 500자 이하여야 합니다."),

    // For test
    FOO(HttpStatus.INTERNAL_SERVER_ERROR, "FOO.001"),
}