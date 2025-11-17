package com.openknot.auth.converter

import com.openknot.auth.exception.ErrorCode
import org.springframework.context.NoSuchMessageException
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

@Configuration
class MessageConverter {
    private val messageSource: YamlMessageSource = YamlMessageSource()

    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun getMessage(
        code: String,
        vararg args: Any?,
    ): String {
        return try {
            messageSource.getMessage(code, args, locale)
        } catch (e: NoSuchMessageException) {
            messageSource.getMessage(ErrorCode.INVALID_ERROR_CODE.code, args, locale)
        }
    }
}