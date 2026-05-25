package com.innowise.userservice.exception.handler;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorResponse {
    private String message;
    private Integer status;
}
