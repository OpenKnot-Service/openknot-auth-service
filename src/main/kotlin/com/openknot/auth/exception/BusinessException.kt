package com.openknot.auth.exception

class BusinessException(
    val errorCode: ErrorCode,
    vararg args: Any?,
) : RuntimeException()