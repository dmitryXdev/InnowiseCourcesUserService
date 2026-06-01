package com.innowise.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidationResponseDto {
    private boolean valid;
    private Long userId;
    private String role;
}
