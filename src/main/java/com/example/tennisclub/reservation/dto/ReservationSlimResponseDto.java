package com.example.tennisclub.reservation.dto;

import com.example.tennisclub.court.dto.CourtResponseDto;

import java.time.LocalDateTime;

public record ReservationSlimResponseDto(
        Long id,
        CourtResponseDto court,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isDoubles,
        Double totalPrice
) implements ReservationView {}
