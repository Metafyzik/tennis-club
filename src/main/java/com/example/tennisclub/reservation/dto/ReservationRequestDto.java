package com.example.tennisclub.reservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequestDto(

        @NotNull(message = "Court ID must not be null")
        @Min(value = 1, message = "Court must not be null and greater than 0") Long courtId,
        @NotNull(message = "isDoubles must be provided")
        Boolean isDoubles,
        @NotBlank(message = "PhoneNumber name must not be blank")
        String phoneNumber,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        LocalDateTime start,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime end) {
}