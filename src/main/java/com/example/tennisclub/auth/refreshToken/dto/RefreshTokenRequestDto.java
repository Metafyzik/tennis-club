package com.example.tennisclub.auth.refreshToken.dto;

import jakarta.validation.constraints.NotBlank;
public record RefreshTokenRequestDto(
        @NotBlank(message = "refreshToken name must not be blank")String refreshToken
) {}