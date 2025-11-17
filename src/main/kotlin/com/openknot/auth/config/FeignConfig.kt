package com.openknot.auth.config

import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignConfig {

    /**
     * OpenFeign이 응답을 객체로 변환할 때 사용하는 HttpMessageConverters를
     * 수동으로 빈 등록하여 WebFlux 환경에서도 사용할 수 있도록 합니다.
     */
    @Bean
    fun httpMessageConverters(): HttpMessageConverters {
        // Spring Boot의 기본 컨버터들(Jackson 등)을 포함하여 생성합니다.
        return HttpMessageConverters()
    }
}