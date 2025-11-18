package com.openknot.auth.handler

import com.openknot.auth.converter.MessageConverter
import com.openknot.auth.dto.ErrorResponse
import com.openknot.auth.exception.BusinessException
import com.openknot.auth.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler(
    private val messageConverter: MessageConverter,
) {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val code = errorCode.code
        val body = ErrorResponse(
            code = code,
            message = messageConverter.getMessage(code),
        )
        logger.error { "Business Exception: $errorCode" }
        return ResponseEntity.status(errorCode.status).body(body)
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(je: JwtException): ResponseEntity<ErrorResponse> {
        val errorCode = when (je) {
            // 토큰 만료
            is ExpiredJwtException -> ErrorCode.TOKEN_EXPIRED
            // 토큰 서명 오류
            is SignatureException -> ErrorCode.TOKEN_SIGNATURE_INVALID
            // 토큰 형식이 잘못되었을 때
            is MalformedJwtException -> ErrorCode.TOKEN_MALFORMED
            // 지원되지 않는 형식의 토큰
            is UnsupportedJwtException -> ErrorCode.TOKEN_UNSUPPORTED
            // 그외 모든 JWT 관련 예외
            else -> ErrorCode.TOKEN_INVALID
        }
        val code = errorCode.code
        val body = ErrorResponse(
            code = code,
            message = messageConverter.getMessage(code),
        )

        logger.error { "JWT Exception: $errorCode" }
        return ResponseEntity.status(errorCode.status).body(body)
    }
}