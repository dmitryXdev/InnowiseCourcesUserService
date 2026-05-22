package com.innowise.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotNull
    @Past
    private LocalDate birthDate;
    @NotBlank
    @Email
    private String email;
    @NotNull
    private Boolean active;
    private List<CardDto> cards;
    @NotNull
    private LocalDateTime createdAt;
}
