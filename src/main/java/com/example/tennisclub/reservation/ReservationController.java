package com.example.tennisclub.reservation;

import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.reservation.dto.ReservationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/{id}")
    public ReservationResponseDto getReservation(@PathVariable Long id) {
        return reservationService.getReservation(id);

    }

    @GetMapping("/by-court/{courtId}")
    public List<ReservationResponseDto> getReservationsByCourt(@PathVariable Long courtId) {
        return reservationService.getReservationsByCourt(courtId);
    }

    @GetMapping
    public List<ReservationResponseDto> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/by-phone")
    public List<ReservationResponseDto> getReservationsByPhone(
            @RequestParam String phoneNumber,
            @RequestParam(defaultValue = "false") boolean futureOnly) {
        return reservationService.getReservationsByPhoneNumber(phoneNumber, futureOnly);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> create(@RequestBody @Valid ReservationRequestDto req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.create(req));

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponseDto update(@PathVariable @Valid Long id, @RequestBody ReservationRequestDto req) {
        return reservationService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        reservationService.softDelete(id);

        return ResponseEntity.ok("Reservation with id "+ id + " deleted successfully.");
    }


}