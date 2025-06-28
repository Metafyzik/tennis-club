package com.example.tennisclub.reservation;

import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.reservation.config.PricingProperties;
import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.reservation.dto.ReservationResponseDto;
import com.example.tennisclub.reservation.entity.Reservation;
import com.example.tennisclub.reservation.validator.ReservationValidator;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import com.example.tennisclub.user.UserService;
import com.example.tennisclub.user.dto.UserResponseDto;
import com.example.tennisclub.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final EntityFinder entityFinder;
    private final CourtService courtService;
    private final UserService userService;
    private final PricingProperties pricing;
    public ReservationResponseDto getReservation(Long id) {
        Reservation reservation = findReservationEntityByIdOrThrow(id);

        return mapToResponseDto(reservation);
    }

    public Reservation findReservationEntityByIdOrThrow(Long id) {
        return entityFinder.findByIdOrThrow(
                reservationRepo.findById(id), id, "Reservation");
    }

    public List<Reservation> findAllReservationEntities() {
        return reservationRepo.findAll();
    }

    public List<Reservation> findAllReservationEntitiesByCourtId(Long courtId) {
        courtService.findCourtEntityByIdOrThrow(courtId);
        return reservationRepo.findAllByCourtId(courtId);
    }

    public List<Reservation> findReservationsByPhoneNumber(String phoneNumber, boolean futureOnly) {
        userService.findByPhoneNumberOrThrow(phoneNumber);
        return reservationRepo.findByPhoneNumber(phoneNumber, futureOnly);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return findAllReservationEntities().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public List<ReservationResponseDto> getReservationsByCourt(Long courtId) {
        return findAllReservationEntitiesByCourtId(courtId).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public List<ReservationResponseDto> getReservationsByPhoneNumber(String phoneNumber, boolean futureOnly) {
        return findReservationsByPhoneNumber(phoneNumber, futureOnly).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional
    public ReservationResponseDto create(ReservationRequestDto dto) {
        Reservation reservation = prepareNewReservation(dto);
        return mapToResponseDto(reservationRepo.save(reservation));
    }
    private Reservation prepareNewReservation(ReservationRequestDto dto) {
        Court court = courtService.findCourtEntityByIdOrThrow(dto.courtId());
        ReservationValidator.validateStartBeforeEnd(dto.start(), dto.end());

        List<Reservation> overlaps = findConflicts(court.getId(), dto.start(), dto.end());
        ReservationValidator.throwIfOverlapsExist(overlaps);

        // Use the authenticated username to find user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsernameOrThrow(username);

        double totalPrice = calculatePrice(dto.isDoubles(), dto.start(), dto.end(), court.getSurfaceType().getPricePerMinute());

        return Reservation.builder()
                .court(court)
                .isDoubles(dto.isDoubles())
                .user(user)
                .startTime(dto.start())
                .endTime(dto.end())
                .totalPrice(totalPrice)
                .build();
    }

    public List<Reservation> findConflicts(Long courtId, LocalDateTime from, LocalDateTime to) {
        return reservationRepo.findOverlappingReservations(courtId, from, to);
    }
    @Transactional
    public ReservationResponseDto update(Long reservationId, ReservationRequestDto updated) {
        Reservation existing = findReservationEntityByIdOrThrow(reservationId);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!existing.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not allowed to modify this reservation");
        }

        LocalDateTime newStart = updated.start();
        LocalDateTime newEnd = updated.end();

        //in case of User asking for different court
        Court court = courtService.findCourtEntityByIdOrThrow(updated.courtId());

        List<Reservation> overlaps = findConflicts(court.getId(), updated.start(), updated.end());

        //exclude reservation being updated
        overlaps.removeIf(r -> r.getId().equals(existing.getId()));
        ReservationValidator.throwIfOverlapsExist(overlaps);

        // in case user is changed
        User user = userService.findByPhoneNumberOrThrow(updated.phoneNumber());
        double totalPrice = calculatePrice(updated.isDoubles(), updated.start(), updated.end(), court.getSurfaceType().getPricePerMinute());

        existing.setUser(user);
        existing.setStartTime(newStart);
        existing.setEndTime(newEnd);
        existing.setIsDoubles(updated.isDoubles());
        existing.setCourt(court);
        existing.setTotalPrice(totalPrice);

        Reservation updatedReservation = reservationRepo.update(existing);
        return mapToResponseDto(updatedReservation);
    }

    @Transactional
    public void softDelete(Long id) {

        Reservation existing = findReservationEntityByIdOrThrow(id);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!existing.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not allowed to modify this reservation");
        }

        reservationRepo.softDelete(id);
    }

    private ReservationResponseDto mapToResponseDto(Reservation r) {
        Court court = r.getCourt();
        SurfaceType st = court.getSurfaceType();
        User user = r.getUser();

        SurfaceTypeResponseDto stDto = new SurfaceTypeResponseDto(st.getId(), st.getName(), st.getPricePerMinute());
        CourtResponseDto courtDto = new CourtResponseDto(court.getId(), court.getName(), stDto);
        UserResponseDto userDto = new UserResponseDto(user.getId(), user.getPhoneNumber(), user.getUsername());

        return new ReservationResponseDto(
                r.getId(),
                courtDto,
                userDto,
                r.getStartTime(),
                r.getEndTime(),
                r.getIsDoubles(),
                r.getTotalPrice()
        );
    }
    private double calculatePrice(boolean isDouble, LocalDateTime start, LocalDateTime end, double pricePerSurfaceType) {
        long minutes = Duration.between(start, end).toMinutes();
        return pricePerSurfaceType * minutes * (isDouble ? pricing.getDoubles(): 1);
    }


    //used for data initialization
    @Transactional
    public Reservation createForUser(ReservationRequestDto dto, User user) {
        Court court = courtService.findCourtEntityByIdOrThrow(dto.courtId());
        ReservationValidator.validateStartBeforeEnd(dto.start(), dto.end());

        List<Reservation> overlaps = findConflicts(court.getId(), dto.start(), dto.end());
        ReservationValidator.throwIfOverlapsExist(overlaps);

        double totalPrice = calculatePrice(dto.isDoubles(), dto.start(), dto.end(), court.getSurfaceType().getPricePerMinute());

        Reservation reservation = Reservation.builder()
                .court(court)
                .isDoubles(dto.isDoubles())
                .user(user)
                .startTime(dto.start())
                .endTime(dto.end())
                .totalPrice(totalPrice)
                .build();

        return reservationRepo.save(reservation);
    }
}
