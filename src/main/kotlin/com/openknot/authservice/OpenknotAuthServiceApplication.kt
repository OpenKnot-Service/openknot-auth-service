package com.openknot.authservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenknotAuthServiceApplication

fun main(args: Array<String>) {
    runApplication<OpenknotAuthServiceApplication>(*args)
}
