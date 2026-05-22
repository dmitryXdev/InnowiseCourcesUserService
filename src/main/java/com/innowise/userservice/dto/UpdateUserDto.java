package com.innowise.userservice.dto;

import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserDto {
    private String name;
    private String surname;
    @Past
    private LocalDate birthDate;
}
