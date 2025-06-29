package com.example.tennisclub.reservationTests;

import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.reservation.ReservationRepository;
import com.example.tennisclub.reservation.entity.Reservation;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationRepositoryTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Reservation> typedQuery;
    @InjectMocks
    private ReservationRepository reservationRepository;

    private Reservation testReservation;
    private Court testCourt;
    private User testUser;
    private SurfaceType testSurfaceType;

    @BeforeEach
    void setUp() {
        testSurfaceType = SurfaceType.builder()
                .id(1L)
                .name("Clay")
                .pricePerMinute(0.5)
                .deleted(false)
                .build();

        testCourt = Court.builder()
                .id(1L)
                .name("Court 1")
                .surfaceType(testSurfaceType)
                .deleted(false)
                .build();

        testUser = User.builder()
                .id(1L)
                .phoneNumber("+420123456789")
                .username("testuser")
                .password("password")
                .deleted(false)
                .roles(Set.of(Role.MEMBER))
                .build();

        testReservation = Reservation.builder()
                .id(1L)
                .court(testCourt)
                .user(testUser)
                .startTime(LocalDateTime.of(2024, 6, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 6, 15, 11, 0))
                .isDoubles(false)
                .totalPrice(30.0)
                .deleted(false)
                .build();
    }

    @Nested
    class FindReservationsByIdTests {

        @Test
        void shouldReturnReservation_WhenExistsAndNotDeleted() {
            when(entityManager.find(Reservation.class, 1L)).thenReturn(testReservation);

            Optional<Reservation> result = reservationRepository.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(testReservation, result.get());
        }

        @Test
        void shouldReturnEmpty_WhenReservationDoesNotExist() {
            when(entityManager.find(Reservation.class, 1L)).thenReturn(null);

            Optional<Reservation> result = reservationRepository.findById(1L);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnEmpty_WhenReservationIsDeleted() {
            testReservation.setDeleted(true);
            when(entityManager.find(Reservation.class, 1L)).thenReturn(testReservation);

            Optional<Reservation> result = reservationRepository.findById(1L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindAllReservationsByCourtIdTests {

        @Test
        void shouldReturnNonDeletedReservations() {
            List<Reservation> expectedReservations = List.of(testReservation);

            when(entityManager.createQuery(anyString(), eq(Reservation.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("courtId", 1L)).thenReturn(typedQuery);

            when(typedQuery.getResultList()).thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findAllByCourtId(1L);

            assertEquals(expectedReservations, result);
        }
    }

    @Nested
    class FindReservationsByPhoneNumberTests {

        @Test
        void withFutureOnlyTrue_ShouldIncludeFutureFilter() {
            List<Reservation> expectedReservations = List.of(testReservation);
            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

            when(entityManager.createQuery(queryCaptor.capture(), eq(Reservation.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("phone", testUser.getPhoneNumber())).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findByPhoneNumber(testUser.getPhoneNumber(), true);

            assertEquals(expectedReservations, result);

            String actualQuery = queryCaptor.getValue();
            assertTrue(actualQuery.contains("r.startTime > CURRENT_TIMESTAMP"), "Query should filter future reservations");
        }


        @Test
        void withFutureOnlyFalse_ShouldNotIncludeFutureFilter() {
            List<Reservation> expectedReservations = List.of(testReservation);
            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

            when(entityManager.createQuery(queryCaptor.capture(), eq(Reservation.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("phone", testUser.getPhoneNumber())).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findByPhoneNumber(testUser.getPhoneNumber(), false);

            assertEquals(expectedReservations, result);

            String actualQuery = queryCaptor.getValue();
            assertFalse(actualQuery.contains("r.startTime > CURRENT_TIMESTAMP"), "Query should not filter by future reservations");
        }
    }

    @Nested
    class FindOverlappingReservationsTests {

        @Test
        void shouldReturnOverlappingReservations() {
            LocalDateTime from = LocalDateTime.of(2024, 6, 15, 9, 30);
            LocalDateTime to = LocalDateTime.of(2024, 6, 15, 10, 30);
            List<Reservation> expectedReservations = List.of(testReservation);

            when(entityManager.createQuery(anyString(), eq(Reservation.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("courtId", 1L)).thenReturn(typedQuery);
            when(typedQuery.setParameter("from", from)).thenReturn(typedQuery);
            when(typedQuery.setParameter("to", to)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findOverlappingReservations(1L, from, to);

            assertEquals(expectedReservations, result);
        }
    }

    @Nested
    class FindAllReservationsTests {

        @Test
        void shouldReturnOnlyNonDeletedReservations() {
            List<Reservation> expectedReservations = List.of(testReservation);
            when(entityManager.createQuery(anyString(), eq(Reservation.class))).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findAll();

            assertEquals(expectedReservations, result);
        }
    }

    @Nested
    class SaveReservationTests {

        @Test
        void shouldPersistReservation() {
            Reservation result = reservationRepository.save(testReservation);

            assertEquals(testReservation, result);
            verify(entityManager).persist(testReservation);
        }
    }

    @Nested
    class UpdateReservationTests {

        @Test
        void shouldMergeReservation() {
            when(entityManager.merge(testReservation)).thenReturn(testReservation);

            Reservation result = reservationRepository.update(testReservation);

            assertEquals(testReservation, result);
        }
    }

    @Nested
    class SoftDeleteReservationTests {

        @Test
        void shouldReturnTrue_WhenReservationExistsAndNotDeleted() {
            when(entityManager.find(Reservation.class, 1L)).thenReturn(testReservation);

            boolean result = reservationRepository.softDelete(1L);

            assertTrue(result);
            assertTrue(testReservation.getDeleted());
        }

        @Test
        void shouldReturnFalse_WhenReservationDoesNotExist() {
            when(entityManager.find(Reservation.class, 1L)).thenReturn(null);

            boolean result = reservationRepository.softDelete(1L);

            assertFalse(result);
        }

        @Test
        void shouldReturnFalse_WhenReservationIsAlreadyDeleted() {
            testReservation.setDeleted(true);
            when(entityManager.find(Reservation.class, 1L)).thenReturn(testReservation);

            boolean result = reservationRepository.softDelete(1L);

            assertFalse(result);
        }
    }

    @Nested
    class FindReservationsByUsernameTests {

        @Test
        void withFutureOnlyTrue_ShouldIncludeFutureFilter() {
            List<Reservation> expectedReservations = List.of(testReservation);
            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

            when(entityManager.createQuery(queryCaptor.capture(), eq(Reservation.class)))
                    .thenReturn(typedQuery);
            when(typedQuery.setParameter("username", testUser.getUsername()))
                    .thenReturn(typedQuery);
            when(typedQuery.getResultList())
                    .thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findByUsername(testUser.getUsername(), true);

            assertEquals(expectedReservations, result);

            String actualQuery = queryCaptor.getValue();
            assertTrue(actualQuery.contains("r.startTime > CURRENT_TIMESTAMP"),
                    "Query should filter future reservations");
            assertTrue(actualQuery.contains("r.user.username = :username"),
                    "Query should filter by username");
        }

        @Test
        void withFutureOnlyFalse_ShouldNotIncludeFutureFilter() {
            List<Reservation> expectedReservations = List.of(testReservation);
            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

            when(entityManager.createQuery(queryCaptor.capture(), eq(Reservation.class)))
                    .thenReturn(typedQuery);
            when(typedQuery.setParameter("username", testUser.getUsername()))
                    .thenReturn(typedQuery);
            when(typedQuery.getResultList())
                    .thenReturn(expectedReservations);

            List<Reservation> result = reservationRepository.findByUsername(testUser.getUsername(), false);

            assertEquals(expectedReservations, result);

            String actualQuery = queryCaptor.getValue();
            assertFalse(actualQuery.contains("r.startTime > CURRENT_TIMESTAMP"),
                    "Query should not filter by future reservations");
            assertTrue(actualQuery.contains("r.user.username = :username"),
                    "Query should filter by username");
        }
    }
}