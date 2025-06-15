package com.example.tennisclub.auth.dto;

//Response both for authentication and token refresh
public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {}
