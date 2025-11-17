package com.openknot.auth.feign.client

import com.openknot.auth.dto.UserIdResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "user-service",
    url = "\${feign.user.url}",
)
interface UserClient {

    @GetMapping("/user-id")
    fun getUserId(@RequestParam(value = "email") email: String, @RequestParam(value = "password") password: String): UserIdResponse
}