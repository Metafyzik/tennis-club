package com.example.tennisclub.reservation.dto;

import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.reservation.dto.ReservationResponseDto;
import com.example.tennisclub.reservation.dto.ReservationSlimResponseDto;

import java.time.LocalDateTime;

public sealed interface ReservationView permits ReservationResponseDto, ReservationSlimResponseDto {
    Long id();
    CourtResponseDto court();
    LocalDateTime startTime();
    LocalDateTime endTime();
    boolean isDoubles();
    Double totalPrice();
}

