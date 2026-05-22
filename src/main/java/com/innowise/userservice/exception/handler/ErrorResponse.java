package com.innowise.userservice.exception.handler;

import lombok.Builder;

@Builder
public class ErrorResponse {
    private String message;
    private Integer status;
}
