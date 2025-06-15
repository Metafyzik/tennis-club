package com.example.tennisclub.reservation.validator;

import com.example.tennisclub.reservation.entity.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationValidator {
    public static void validateStartBeforeEnd(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time must be before end time"
            );
        }
    }

    public static void throwIfOverlapsExist(List<Reservation> overlaps) {
        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Court is already reserved during the selected time period"
            );
        }
    }
}