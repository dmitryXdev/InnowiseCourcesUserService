package com.innowise.userservice.feign;

import com.innowise.userservice.config.FeignConfig;
import com.innowise.userservice.dto.TokenValidationRequestDto;
import com.innowise.userservice.dto.TokenValidationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "${services.auth.url}", configuration = FeignConfig.class)
public interface AuthClient {
    @PostMapping("/auth/validate")
    TokenValidationResponseDto validate(@RequestBody TokenValidationRequestDto dto);
}
