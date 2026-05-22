package com.innowise.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCardDto {
    @Pattern(regexp = "^[23456]\\d{15}$")
    private String number;
    @Future
    private LocalDate expirationDate;
}
