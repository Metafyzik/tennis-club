package com.example.tennisclub.unitTest.reservationTests;

import com.example.tennisclub.reservation.entity.Reservation;
import com.example.tennisclub.reservation.validator.ReservationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationValidatorTest {

    @Test
    void validateStartBeforeEnd_WhenStartIsBeforeEnd_ShouldNotThrowException() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 6, 15, 11, 0);

        assertDoesNotThrow(() -> ReservationValidator.validateStartBeforeEnd(start, end));
    }

    @Test
    void validateStartBeforeEnd_WhenStartIsAfterEnd_ShouldThrowBadRequestException() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 15, 11, 0);
        LocalDateTime end = LocalDateTime.of(2024, 6, 15, 10, 0);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ReservationValidator.validateStartBeforeEnd(start, end)
        );

        assertEquals("400 BAD_REQUEST \"Start time must be before end time\"", exception.getMessage());
    }

    @Test
    void validateStartBeforeEnd_WhenStartEqualsEnd_ShouldThrowBadRequestException() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 6, 15, 10, 0);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ReservationValidator.validateStartBeforeEnd(start, end)
        );

        assertEquals("400 BAD_REQUEST \"Start time must be before end time\"", exception.getMessage());
    }

    @Test
    void throwIfOverlapsExist_WhenOverlapsListIsEmpty_ShouldNotThrowException() {
        List<Reservation> emptyOverlaps = new ArrayList<>();

        assertDoesNotThrow(() -> ReservationValidator.throwIfOverlapsExist(emptyOverlaps));
    }

    @Test
    void throwIfOverlapsExist_WhenOverlapsExist_ShouldThrowConflictException() {
        Reservation dummyReservation = Reservation.builder().id(1L).build();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ReservationValidator.throwIfOverlapsExist(List.of(dummyReservation))
        );

        assertEquals("409 CONFLICT \"Court is already reserved during the selected time period\"", ex.getMessage());
        assertEquals(409, ex.getStatusCode().value());
    }
}