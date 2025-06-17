package com.example.tennisclub.reservationTests;
import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.reservation.ReservationRepository;
import com.example.tennisclub.reservation.ReservationService;
import com.example.tennisclub.reservation.config.PricingProperties;
import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.reservation.dto.ReservationResponseDto;
import com.example.tennisclub.reservation.entity.Reservation;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import com.example.tennisclub.user.UserService;
import com.example.tennisclub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepo;

    @Mock
    private EntityFinder entityFinder;

    @Mock
    private CourtService courtService;

    @Mock
    private UserService userService;

    @Mock
    private PricingProperties pricingProperties;

    @InjectMocks
    private ReservationService reservationService;

    private Court sampleCourt;
    private User sampleUser;
    private Reservation sampleReservation;
    private ReservationRequestDto sampleRequestDto;

    @BeforeEach
    void setUp() {
        // Create sample entities
        SurfaceType surfaceType = SurfaceType.builder()
                .id(1L)
                .name("Clay")
                .pricePerMinute(10.0)
                .build();

        sampleCourt = Court.builder()
                .id(1L)
                .name("Court 1")
                .surfaceType(surfaceType)
                .build();

        sampleUser = User.builder()
                .id(1L)
                .phoneNumber("+420123456789")
                .username("John Doe")
                .build();

        sampleReservation = Reservation.builder()
                .id(1L)
                .court(sampleCourt)
                .user(sampleUser)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isDoubles(false)
                .totalPrice(600.0)
                .build();

        sampleRequestDto = new ReservationRequestDto(
                1L,
                false,
                "+420123456789",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );
    }

    @Nested
    class SingleReservationRetrievalTests {

        @Test
        void getReservation_WhenReservationExists_ShouldReturnResponseDto() {
            Long reservationId = 1L;
            when(entityFinder.findByIdOrThrow(any(), eq(reservationId), eq("Reservation")))
                    .thenReturn(sampleReservation);

            ReservationResponseDto result = reservationService.getReservation(reservationId);

            assertNotNull(result);
            assertEquals(sampleReservation.getId(), result.id());
            assertEquals(sampleCourt.getId(), result.court().id());
            assertEquals(sampleUser.getId(), result.user().id());
            verify(entityFinder).findByIdOrThrow(any(), eq(reservationId), eq("Reservation"));
        }

        @Test
        void findReservationEntityByIdOrThrow_WhenReservationDoesNotExist_ShouldThrowNotFound() {
            Long reservationId = 42L;
            String expectedMessage = "Reservation with ID " + reservationId + " not found";

            when(reservationRepo.findById(reservationId)).thenReturn(Optional.empty());
            when(entityFinder.findByIdOrThrow(Optional.empty(), reservationId, "Reservation"))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.findReservationEntityByIdOrThrow(reservationId)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals(expectedMessage, exception.getReason());

            verify(reservationRepo).findById(reservationId);
            verify(entityFinder).findByIdOrThrow(Optional.empty(), reservationId, "Reservation");
        }

        @Test
        void findReservationEntityByIdOrThrow_WhenReservationExists_ShouldReturnReservation() {
            Long reservationId = 1L;
            when(reservationRepo.findById(reservationId)).thenReturn(Optional.of(sampleReservation));
            when(entityFinder.findByIdOrThrow(any(), eq(reservationId), eq("Reservation")))
                    .thenReturn(sampleReservation);

            Reservation result = reservationService.findReservationEntityByIdOrThrow(reservationId);

            assertNotNull(result);
            assertEquals(sampleReservation.getId(), result.getId());
            verify(entityFinder).findByIdOrThrow(any(), eq(reservationId), eq("Reservation"));
        }
    }

    @Nested
    class MultipleReservationRetrievalTests {

        @Test
        void findAllReservationEntities_ShouldReturnAllReservations() {
            List<Reservation> reservations = Arrays.asList(sampleReservation);
            when(reservationRepo.findAll()).thenReturn(reservations);

            List<Reservation> result = reservationService.findAllReservationEntities();

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).getId());
            verify(reservationRepo).findAll();
        }

        @Test
        void getAllReservations_ShouldReturnResponseDtoList() {
            List<Reservation> reservations = Arrays.asList(sampleReservation);
            when(reservationRepo.findAll()).thenReturn(reservations);

            List<ReservationResponseDto> result = reservationService.getAllReservations();

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).id());
            verify(reservationRepo).findAll();
        }
    }

    @Nested
    class ReservationRetrievalByCourtTests {

        @Test
        void findAllReservationEntitiesByCourtId_WhenCourtExists_ShouldReturnReservations() {
            Long courtId = 1L;
            List<Reservation> reservations = Arrays.asList(sampleReservation);
            when(courtService.findCourtEntityByIdOrThrow(courtId)).thenReturn(sampleCourt);
            when(reservationRepo.findAllByCourtId(courtId)).thenReturn(reservations);

            List<Reservation> result = reservationService.findAllReservationEntitiesByCourtId(courtId);

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).getId());
            verify(courtService).findCourtEntityByIdOrThrow(courtId);
            verify(reservationRepo).findAllByCourtId(courtId);
        }

        @Test
        void findAllReservationEntitiesByCourtId_WhenCourtDoesNotExist_ShouldThrowNotFound() {
            Long nonExistingCourtId = 999L;
            when(courtService.findCourtEntityByIdOrThrow(nonExistingCourtId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.findAllReservationEntitiesByCourtId(nonExistingCourtId)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Court not found", exception.getReason());
            verify(courtService).findCourtEntityByIdOrThrow(nonExistingCourtId);
            verifyNoInteractions(reservationRepo);
        }

        @Test
        void getReservationsByCourt_ShouldReturnResponseDtoList() {
            Long courtId = 1L;
            List<Reservation> reservations = Arrays.asList(sampleReservation);
            when(courtService.findCourtEntityByIdOrThrow(courtId)).thenReturn(sampleCourt);
            when(reservationRepo.findAllByCourtId(courtId)).thenReturn(reservations);

            List<ReservationResponseDto> result = reservationService.getReservationsByCourt(courtId);

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).id());
            verify(courtService).findCourtEntityByIdOrThrow(courtId);
            verify(reservationRepo).findAllByCourtId(courtId);
        }
    }

    @Nested
    class findReservationsByPhoneNumberTests {

        @Test
        void findReservationsByPhoneNumber_WhenUserExists_ShouldReturnReservations() {
            String phoneNumber = "+420123456789";
            boolean futureOnly = true;
            List<Reservation> reservations = Arrays.asList(sampleReservation);
            when(userService.findByPhoneNumberOrThrow(phoneNumber)).thenReturn(sampleUser);
            when(reservationRepo.findByPhoneNumber(phoneNumber, futureOnly)).thenReturn(reservations);

            List<Reservation> result = reservationService.findReservationsByPhoneNumber(phoneNumber, futureOnly);

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).getId());
            verify(userService).findByPhoneNumberOrThrow(phoneNumber);
            verify(reservationRepo).findByPhoneNumber(phoneNumber, futureOnly);
        }

        @Test
        void findReservationsByPhoneNumber_WhenUserDoesNotExist_ShouldThrowNotFound() {
            String nonExistingPhoneNumber = "+420987654321";
            boolean futureOnly = true;

            when(userService.findByPhoneNumberOrThrow(nonExistingPhoneNumber))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.findReservationsByPhoneNumber(nonExistingPhoneNumber, futureOnly)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());

            verify(userService).findByPhoneNumberOrThrow(nonExistingPhoneNumber);
            verifyNoInteractions(reservationRepo);
        }

        @Test
        void getReservationsByPhoneNumber_ShouldReturnResponseDtoList() {
            String phoneNumber = "+420123456789";
            boolean futureOnly = false;
            List<Reservation> reservations = Arrays.asList(sampleReservation);
            when(userService.findByPhoneNumberOrThrow(phoneNumber)).thenReturn(sampleUser);
            when(reservationRepo.findByPhoneNumber(phoneNumber, futureOnly)).thenReturn(reservations);

            List<ReservationResponseDto> result = reservationService.getReservationsByPhoneNumber(phoneNumber, futureOnly);

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).id());
            verify(userService).findByPhoneNumberOrThrow(phoneNumber);
            verify(reservationRepo).findByPhoneNumber(phoneNumber, futureOnly);
        }
    }

    @Nested
    class CreateReservationTests {

        @Test
        void create_WithValidRequest_ShouldCreateReservation() {
            when(courtService.findCourtEntityByIdOrThrow(sampleRequestDto.courtId())).thenReturn(sampleCourt);
            when(userService.findByPhoneNumberOrThrow(sampleRequestDto.phoneNumber())).thenReturn(sampleUser);
            when(reservationRepo.findOverlappingReservations(any(), any(), any())).thenReturn(Arrays.asList());
            when(reservationRepo.save(any(Reservation.class))).thenReturn(sampleReservation);

            ReservationResponseDto result = reservationService.create(sampleRequestDto);

            assertNotNull(result);
            assertEquals(sampleReservation.getId(), result.id());
            verify(courtService).findCourtEntityByIdOrThrow(sampleRequestDto.courtId());
            verify(userService).findByPhoneNumberOrThrow(sampleRequestDto.phoneNumber());
            verify(reservationRepo).findOverlappingReservations(any(), any(), any());
            verify(reservationRepo).save(any(Reservation.class));
        }

        @Test
        void create_WithOverlappingReservations_ShouldThrowConflictException() {
            Reservation overlappingReservation = Reservation.builder().id(2L).build();
            when(courtService.findCourtEntityByIdOrThrow(sampleRequestDto.courtId())).thenReturn(sampleCourt);
            when(reservationRepo.findOverlappingReservations(any(), any(), any()))
                    .thenReturn(new ArrayList<>(List.of(overlappingReservation)));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.create(sampleRequestDto)
            );

            assertEquals("409 CONFLICT \"Court is already reserved during the selected time period\"",
                    exception.getMessage());
            verify(courtService).findCourtEntityByIdOrThrow(sampleRequestDto.courtId());
            verify(reservationRepo).findOverlappingReservations(any(), any(), any());
            verify(reservationRepo, never()).save(any());
        }

        @Test
        void create_WithStartAfterEnd_ShouldThrowBadRequestException() {
            ReservationRequestDto invalidDto = new ReservationRequestDto(
                    1L, false, "+420123456789",
                    LocalDateTime.now().plusHours(2), // start after end
                    LocalDateTime.now().plusHours(1)
            );
            when(courtService.findCourtEntityByIdOrThrow(invalidDto.courtId())).thenReturn(sampleCourt);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.create(invalidDto)
            );

            assertEquals("400 BAD_REQUEST \"Start time must be before end time\"",
                    exception.getMessage());
            verify(courtService).findCourtEntityByIdOrThrow(invalidDto.courtId());
            verify(reservationRepo, never()).save(any());
        }
    }

    @Nested
    class UpdateReservationTests {

        @Test
        void update_WithValidRequest_ShouldUpdateReservation() {
            Long reservationId = 1L;
            ReservationRequestDto updateDto = new ReservationRequestDto(
                    1L,  true, "+420123456789",
                    LocalDateTime.now().plusHours(2),
                    LocalDateTime.now().plusHours(3)
            );

            when(entityFinder.findByIdOrThrow(any(), eq(reservationId), eq("Reservation")))
                    .thenReturn(sampleReservation);
            when(courtService.findCourtEntityByIdOrThrow(updateDto.courtId())).thenReturn(sampleCourt);
            when(reservationRepo.findOverlappingReservations(any(), any(), any())).thenReturn(Arrays.asList());

            when(courtService.findCourtEntityByIdOrThrow(updateDto.courtId())).thenReturn(sampleCourt);
            when(userService.findByPhoneNumberOrThrow(sampleRequestDto.phoneNumber())).thenReturn(sampleUser);
            when(reservationRepo.update(any(Reservation.class))).thenReturn(sampleReservation);

            ReservationResponseDto result = reservationService.update(reservationId, updateDto);

            assertNotNull(result);
            verify(entityFinder).findByIdOrThrow(any(), eq(reservationId), eq("Reservation"));
            verify(courtService).findCourtEntityByIdOrThrow(updateDto.courtId());
            verify(reservationRepo).findOverlappingReservations(any(), any(), any());
            verify(reservationRepo).update(any(Reservation.class));
        }

        @Test
        void update_WithOverlappingReservationsExcludingSelf_ShouldUpdateSuccessfully() {
            Long reservationId = 1L;
            Reservation overlappingReservation = Reservation.builder().id(reservationId).build(); // Same ID

            when(entityFinder.findByIdOrThrow(any(), eq(reservationId), eq("Reservation")))
                    .thenReturn(sampleReservation);
            when(courtService.findCourtEntityByIdOrThrow(sampleRequestDto.courtId())).thenReturn(sampleCourt);
            when(reservationRepo.findOverlappingReservations(any(), any(), any()))
                    .thenReturn(new ArrayList<>(Arrays.asList(overlappingReservation)));
            when(userService.findByPhoneNumberOrThrow(sampleRequestDto.phoneNumber())).thenReturn(sampleUser);
            when(reservationRepo.update(any(Reservation.class))).thenReturn(sampleReservation);

            ReservationResponseDto result = reservationService.update(reservationId, sampleRequestDto);

            assertNotNull(result);
            verify(entityFinder).findByIdOrThrow(any(), eq(reservationId), eq("Reservation"));
            verify(courtService).findCourtEntityByIdOrThrow(sampleRequestDto.courtId());
            verify(reservationRepo).findOverlappingReservations(any(), any(), any());
            verify(reservationRepo).update(any(Reservation.class));
        }

        @Test
        void update_WithDifferentOverlappingReservation_ShouldThrowConflictException() {
            Long reservationId = 1L;
            Reservation overlappingReservation = Reservation.builder().id(2L).build(); // Different ID

            when(entityFinder.findByIdOrThrow(any(), eq(reservationId), eq("Reservation")))
                    .thenReturn(sampleReservation);
            when(courtService.findCourtEntityByIdOrThrow(sampleRequestDto.courtId())).thenReturn(sampleCourt);
            when(reservationRepo.findOverlappingReservations(any(), any(), any()))
                    .thenReturn(Arrays.asList(overlappingReservation));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.update(reservationId, sampleRequestDto)
            );

            assertEquals("409 CONFLICT \"Court is already reserved during the selected time period\"",
                    exception.getMessage());
            verify(reservationRepo, never()).update(any());
        }
    }

    @Nested
    class DeleteReservationTests {

        @Test
        void softDelete_WhenReservationExists_ShouldDeleteSuccessfully() {
            Long reservationId = 1L;
            when(reservationRepo.softDelete(reservationId)).thenReturn(true);

            assertDoesNotThrow(() -> reservationService.softDelete(reservationId));

            verify(reservationRepo).softDelete(reservationId);
        }

        @Test
        void softDelete_WhenReservationNotFound_ShouldThrowNotFoundException() {
            Long reservationId = 1L;
            when(reservationRepo.softDelete(reservationId)).thenReturn(false);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> reservationService.softDelete(reservationId)
            );

            assertEquals("404 NOT_FOUND \"Reservation with ID 1 not found\"",
                    exception.getMessage());
            verify(reservationRepo).softDelete(reservationId);
        }
    }

    @Nested
    class ReservationConflictsTests {

        @Test
        void findConflicts_ShouldReturnOverlappingReservations() {
            Long courtId = 1L;
            LocalDateTime from = LocalDateTime.now().plusHours(1);
            LocalDateTime to = LocalDateTime.now().plusHours(2);
            List<Reservation> conflicts = Arrays.asList(sampleReservation);
            when(reservationRepo.findOverlappingReservations(courtId, from, to)).thenReturn(conflicts);

            List<Reservation> result = reservationService.findConflicts(courtId, from, to);

            assertEquals(1, result.size());
            assertEquals(sampleReservation.getId(), result.get(0).getId());
            verify(reservationRepo).findOverlappingReservations(courtId, from, to);
        }
    }

    @Nested
    class PriceCalculationTests {

        @Test
        void calculatePrice_ForSingles_ShouldCalculateCorrectPrice() {
            when(courtService.findCourtEntityByIdOrThrow(sampleRequestDto.courtId())).thenReturn(sampleCourt);
            when(userService.findByPhoneNumberOrThrow(sampleRequestDto.phoneNumber())).thenReturn(sampleUser);
            when(reservationRepo.findOverlappingReservations(any(), any(), any())).thenReturn(Arrays.asList());
            when(reservationRepo.save(any(Reservation.class))).thenAnswer(invocation -> {
                Reservation saved = invocation.getArgument(0);
                // 60 minutes * 10.0 price per minute * 1 (singles multiplier) = 600.0
                assertEquals(600.0, saved.getTotalPrice(), 0.01);
                return saved;
            });

            reservationService.create(sampleRequestDto);

            verify(reservationRepo).save(any(Reservation.class));
        }

        @Test
        void calculatePrice_ForDoubles_ShouldCalculateCorrectPrice() {
            ReservationRequestDto doublesDto = new ReservationRequestDto(
                    1L, true, "+420123456789",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );

            when(pricingProperties.getDoubles()).thenReturn(1.5);
            when(courtService.findCourtEntityByIdOrThrow(doublesDto.courtId())).thenReturn(sampleCourt);
            when(userService.findByPhoneNumberOrThrow(doublesDto.phoneNumber())).thenReturn(sampleUser);
            when(reservationRepo.findOverlappingReservations(any(), any(), any())).thenReturn(Arrays.asList());
            when(reservationRepo.save(any(Reservation.class))).thenAnswer(invocation -> {
                Reservation saved = invocation.getArgument(0);
                // 60 minutes * 10.0 price per minute * 1.5 (doubles multiplier) = 900.0
                assertEquals(900.0, saved.getTotalPrice(), 0.01);
                return saved;
            });

            reservationService.create(doublesDto);

            verify(reservationRepo).save(any(Reservation.class));
        }
    }
}
