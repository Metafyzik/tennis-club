package com.example.tennisclub.reservation.dto;

import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.user.dto.UserResponseDto;

import java.time.LocalDateTime;

public record ReservationResponseDto(
        Long id,
        CourtResponseDto court,
        UserResponseDto user,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isDoubles,
        Double totalPrice
) {}