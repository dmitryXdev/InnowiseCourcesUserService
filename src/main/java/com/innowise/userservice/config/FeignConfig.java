package com.innowise.userservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Value("${internal.secret}")
    private String internalSecret;
    @Value("${internal.secret.header}")
    private String internalHeader;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header(internalHeader, internalSecret);
    }
}
