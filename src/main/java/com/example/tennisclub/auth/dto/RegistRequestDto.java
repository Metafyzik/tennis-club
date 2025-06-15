package com.example.tennisclub.auth.dto;

import jakarta.validation.constraints.NotBlank;
public record RegistRequestDto(
        @NotBlank(message = "Username must not be blank")
        String username,
        @NotBlank(message = "phoneNumber must not be blank")
        String phoneNumber,
        @NotBlank(message = "Password must not be blank")
        String password)
{}
