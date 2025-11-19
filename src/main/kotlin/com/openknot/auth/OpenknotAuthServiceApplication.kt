package com.openknot.auth

import com.openknot.auth.config.TokenProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(TokenProperties::class)
@SpringBootApplication
class OpenknotAuthServiceApplication

fun main(args: Array<String>) {
    runApplication<OpenknotAuthServiceApplication>(*args)
}
