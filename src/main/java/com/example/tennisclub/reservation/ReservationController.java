package com.example.tennisclub.reservation;

import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.reservation.dto.ReservationResponseDto;
import com.example.tennisclub.reservation.dto.ReservationView;
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
    public ResponseEntity<ReservationView> getReservation(@PathVariable Long id) {
        return  ResponseEntity.ok(reservationService.getReservation(id));

    }

    @GetMapping("/by-court/{courtId}")
    public ResponseEntity<List<ReservationView>> getReservationsByCourt(@PathVariable Long courtId) {
        return  ResponseEntity.ok(reservationService.getReservationsByCourt(courtId));
    }

    @GetMapping
    public ResponseEntity<List<ReservationView>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    //todo add show user's only reservations

    @GetMapping("/by-phone")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationView> getReservationsByPhone(
            @RequestParam String phoneNumber,
            @RequestParam(defaultValue = "false") boolean futureOnly) {
        return reservationService.getReservationsByPhoneNumber(phoneNumber, futureOnly);
    }

    @PostMapping
    public ResponseEntity<ReservationView> create(@RequestBody @Valid ReservationRequestDto req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.create(req));

    }

    @PutMapping("/{id}")
    public ReservationView update(@PathVariable @Valid Long id, @RequestBody ReservationRequestDto req) {
        return reservationService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        reservationService.softDelete(id);

        return ResponseEntity.ok("Reservation with id "+ id + " deleted successfully.");
    }

}