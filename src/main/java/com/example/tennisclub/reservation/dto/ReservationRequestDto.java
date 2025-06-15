package com.example.tennisclub.reservation.dto;

import java.time.LocalDateTime;

public record ReservationRequestDto(

        Long courtId,

        Boolean isDoubles,

        String phoneNumber,

        LocalDateTime start,

        LocalDateTime end) {
}