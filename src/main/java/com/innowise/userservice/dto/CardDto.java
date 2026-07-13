package com.innowise.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardDto {
    @NotNull
    private Long id;
    @NotBlank
    @Pattern(regexp = "^[23456]\\d{15}$")
    private String number;
    @NotBlank
    @Pattern(regexp = "^[A-Z]+\\s[A-Z]+$")
    private String holder;
    @NotNull
    private LocalDate expirationDate;
    @NotNull
    private Boolean active;
    @NotNull
    private Long userId;
    @NotNull
    private LocalDate createdAt;
}
