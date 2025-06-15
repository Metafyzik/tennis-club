package com.example.tennisclub.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogRequestDto(
        @NotBlank(message = "Username must not be blank")
        String username,
        @NotBlank(message = "Password must not be blank")
        String password
)
{}
