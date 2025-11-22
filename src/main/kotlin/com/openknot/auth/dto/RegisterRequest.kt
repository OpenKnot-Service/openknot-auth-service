package com.openknot.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]+\$",
        message = "비밀번호는 대문자, 소문자, 숫자, 특수문자(@\$!%*?&)를 각각 1개 이상 포함해야 합니다."
    )
    val password: String,

    @field:NotBlank(message = "이름은 필수 입력 항목입니다.")
    @field:Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    val name: String,

    @field:Pattern(
        regexp = "^https?://.+",
        message = "올바른 URL 형식이 아닙니다."
    )
    val profileImageUrl: String? = null,

    @field:Size(max = 500, message = "소개는 500자 이하여야 합니다.")
    val description: String? = null,

    @field:Pattern(
        regexp = "^https?://.+",
        message = "올바른 URL 형식이 아닙니다."
    )
    val githubLink: String? = null,
)
