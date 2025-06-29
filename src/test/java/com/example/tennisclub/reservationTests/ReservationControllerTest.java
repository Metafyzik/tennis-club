package com.example.tennisclub.reservationTests;

import com.example.tennisclub.auth.security.JwtUtil;
import com.example.tennisclub.auth.security.SecurityConfig;
import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.reservation.ReservationController;
import com.example.tennisclub.reservation.ReservationService;
import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.reservation.dto.ReservationResponseDto;
import com.example.tennisclub.reservation.dto.ReservationView;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import com.example.tennisclub.user.CustomUserDetailsService;
import com.example.tennisclub.user.dto.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;


    @Nested
    @WithMockUser(username = "memberUser", roles = {"MEMBER"})
    class GetMyReservationsTests {

        @Test
        void getMyReservations_whenFutureOnlyFalse() throws Exception {
            ReservationView view = createSampleResponseDto();
            when(reservationService.getReservationsForCurrentUser(false))
                    .thenReturn(List.of(view));

            mockMvc.perform(get("/api/reservations/my"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(view.id()));

            verify(reservationService).getReservationsForCurrentUser(false);
        }

        @Test
        void getMyFutureReservations_whenFutureOnlyTrue() throws Exception {
            ReservationView view = createSampleResponseDto();
            when(reservationService.getReservationsForCurrentUser(true))
                    .thenReturn(List.of(view));

            mockMvc.perform(get("/api/reservations/my")
                            .param("futureOnly", "true"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(view.id()));

            verify(reservationService).getReservationsForCurrentUser(true);
        }

    }


    @Nested
    class GetReservationTests {

        @Test
        @WithMockUser(roles = "MEMBER")
        void getReservation_ShouldReturnReservation() throws Exception {
            Long reservationId = 1L;
            ReservationResponseDto responseDto = createSampleResponseDto();
            when(reservationService.getReservation(reservationId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/reservations/{id}", reservationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(responseDto.id()));

            verify(reservationService).getReservation(reservationId);
        }

        @Test
        void getReservation_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/reservations/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetReservationsByCourtTests {

        @Test
        @WithMockUser(roles = "MEMBER")
        void getReservationsByCourt_ShouldReturnReservations() throws Exception {
            Long courtId = 1L;
            List<ReservationView> reservations = List.of(createSampleResponseDto());
            when(reservationService.getReservationsByCourt(courtId)).thenReturn(reservations);

            mockMvc.perform(get("/api/reservations/by-court/{courtId}", courtId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(reservations.get(0).id()));

            verify(reservationService).getReservationsByCourt(courtId);
        }


        @Test
        @WithMockUser(roles = "MEMBER")
        void getReservationsByCourt_CourtNotFound_ShouldReturn404() throws Exception {
            when(reservationService.getReservationsByCourt(998L))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Court with ID 998 not found"));

            mockMvc.perform(get("/api/reservations/by-court/{courtId}", 998))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Court with ID 998 not found"));

            verify(reservationService).getReservationsByCourt(998L);
        }
    }

    @Nested
    class GetAllReservationsTests {

        @Test
        @WithMockUser(roles = "MEMBER")
        void getAllReservations_ShouldReturnAllReservations() throws Exception {
            List<ReservationView> reservations = List.of(createSampleResponseDto());
            when(reservationService.getAllReservations()).thenReturn(reservations);

            mockMvc.perform(get("/api/reservations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(reservationService).getAllReservations();
        }
    }


    @Nested
    class GetReservationsByPhoneTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void getReservationsByPhone_WithDefaultFutureOnly_ShouldReturnReservations() throws Exception {
            String phoneNumber = "+420123456789";
            List<ReservationView> reservations = List.of(createSampleResponseDto());
            when(reservationService.getReservationsByPhoneNumber(phoneNumber, false))
                    .thenReturn(reservations);

            mockMvc.perform(get("/api/reservations/by-phone")
                            .param("phoneNumber", phoneNumber))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(reservationService).getReservationsByPhoneNumber(phoneNumber, false);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getReservationsByPhone_UserNotFound_ShouldReturn404() throws Exception {
            String phoneNumber = "+420000000000";

            when(reservationService.getReservationsByPhoneNumber(phoneNumber, false))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with phone number " + phoneNumber + " not found"));

            mockMvc.perform(get("/api/reservations/by-phone")
                            .param("phoneNumber", phoneNumber))
                    .andExpect(status().isNotFound());

            verify(reservationService).getReservationsByPhoneNumber(phoneNumber, false);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getReservationsByPhone_WithFutureOnlyTrue_ShouldReturnFutureReservations() throws Exception {
            String phoneNumber = "+420123456789";
            List<ReservationView> reservations = List.of(createSampleResponseDto());
            when(reservationService.getReservationsByPhoneNumber(phoneNumber, true))
                    .thenReturn(reservations);

            mockMvc.perform(get("/api/reservations/by-phone")
                            .param("phoneNumber", phoneNumber)
                            .param("futureOnly", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(reservationService).getReservationsByPhoneNumber(phoneNumber, true);
        }
    }

    @Nested
    class CreateReservationTests {

        @Test
        @WithMockUser(roles = "MEMBER")
        void create_WithValidRequest_ShouldCreateReservation() throws Exception {
            ReservationRequestDto requestDto = createSampleRequestDto();
            ReservationResponseDto responseDto = createSampleResponseDto();
            when(reservationService.create(any(ReservationRequestDto.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(responseDto.id()));

            verify(reservationService).create(any(ReservationRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_CourtNotFound_Returns404WithMessage() throws Exception {
            Long invalidCourtId = 997L;
            String expectedMessage = "Court with ID " + invalidCourtId + " not found";

            ReservationRequestDto requestWithInvalidCourt = new ReservationRequestDto(
                    invalidCourtId,
                    false,
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );

            when(reservationService.create(any(ReservationRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithInvalidCourt)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(404));

            verify(reservationService).create(any(ReservationRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void create_OverlappingReservation_Returns409WithMessage() throws Exception {
            String expectedMessage = "Court is already reserved during the selected time period.";

            ReservationRequestDto requestDto = createSampleRequestDto();
            when(reservationService.create(any(ReservationRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, expectedMessage));

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(409));

            verify(reservationService).create(any(ReservationRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void create_UserNotFound_Returns404WithMessage() throws Exception {
            String phoneNumber = "+420123456789";
            String expectedMessage = "User with phone number " + phoneNumber + " not found";

            ReservationRequestDto requestWithInvalidUser = new ReservationRequestDto(
                    1L,
                    false,
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );

            when(reservationService.create(any(ReservationRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithInvalidUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(404));

            verify(reservationService).create(any(ReservationRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void create_MissingFields_ShouldReturnBadRequest() throws Exception {
            ReservationRequestDto invalidRequest = new ReservationRequestDto(
                    null, null, null, null
            );

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class UpdateReservationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_WithAdminRole_ShouldUpdateReservation() throws Exception {
            Long reservationId = 1L;
            ReservationRequestDto requestDto = createSampleRequestDto();
            ReservationResponseDto responseDto = createSampleResponseDto();
            when(reservationService.update(eq(reservationId), any(ReservationRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/api/reservations/{id}", reservationId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(responseDto.id()));

            verify(reservationService).update(eq(reservationId), any(ReservationRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_ReservationNotFound_Returns404WithMessage() throws Exception {
            Long nonExistentId = 999L;
            String expectedMessage = "Reservation with ID " + nonExistentId + " not found";
            ReservationRequestDto requestDto = createSampleRequestDto();

            when(reservationService.update(eq(nonExistentId), any(ReservationRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            mockMvc.perform(put("/api/reservations/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(404));

            verify(reservationService).update(nonExistentId, requestDto);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_CourtNotFound_Returns404WithMessage() throws Exception {
            Long reservationId = 1L;
            Long invalidCourtId = 997L;
            String expectedMessage = "Court with ID " + invalidCourtId + " not found";

            ReservationRequestDto requestWithInvalidCourt = new ReservationRequestDto(
                    invalidCourtId,
                    false,
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );

            when(reservationService.update(eq(reservationId), any(ReservationRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            mockMvc.perform(put("/api/reservations/{id}", reservationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithInvalidCourt)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(404));

            verify(reservationService).update(reservationId, requestWithInvalidCourt);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_TimeSlotConflict_Returns409WithMessage() throws Exception {
            Long reservationId = 1L;
            String expectedMessage = "Court is already reserved during the selected time period.";

            ReservationRequestDto requestDto = createSampleRequestDto();
            when(reservationService.update(eq(reservationId), any(ReservationRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, expectedMessage));

            mockMvc.perform(put("/api/reservations/{id}", reservationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(409));

            verify(reservationService).update(reservationId, requestDto);
        }
    }

    @Nested
    class DeleteReservationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void delete_WithAdminRole_ShouldDeleteReservation() throws Exception {
            Long reservationId = 1L;

            mockMvc.perform(delete("/api/reservations/{id}", reservationId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Reservation with id " + reservationId + " deleted successfully."));

            verify(reservationService).softDelete(reservationId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void delete_ReservationNotFound_Returns404WithMessage() throws Exception {
            Long nonExistentId = 999L;
            String expectedMessage = "Reservation with ID " + nonExistentId + " not found";

            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage))
                    .when(reservationService).softDelete(nonExistentId);

            mockMvc.perform(delete("/api/reservations/{id}", nonExistentId)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(expectedMessage))
                    .andExpect(jsonPath("$.status").value(404));

            verify(reservationService).softDelete(nonExistentId);
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void delete_PastReservation_ReturnsBadRequest() throws Exception {
            Long reservationId = 1L;

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete a past reservation."))
                    .when(reservationService).softDelete(reservationId);

            mockMvc.perform(delete("/api/reservations/{id}", reservationId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("You cannot delete a past reservation."));

            verify(reservationService, times(1)).softDelete(reservationId);
        }

        @WithMockUser(username = "someUser", roles = {"MEMBER"})
        @Test
        void delete_WhenUserIsNotOwner_ReturnForbidden() throws Exception {
            Long reservationId = 1L;

            doThrow(new AccessDeniedException("You are not allowed to delete this reservation"))
                    .when(reservationService).softDelete(reservationId);

            mockMvc.perform(delete("/api/reservations/{id}", reservationId)
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Forbidden - You are not allowed to delete this reservation"));

            verify(reservationService, times(1)).softDelete(reservationId);
        }
    }

    private ReservationRequestDto createSampleRequestDto() {
        return new ReservationRequestDto(
                1L,
                false,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );
    }

    private ReservationResponseDto createSampleResponseDto() {
        SurfaceTypeResponseDto surfaceType = new SurfaceTypeResponseDto(1L, "Clay", 10.0);
        CourtResponseDto court = new CourtResponseDto(1L, "Court 1", surfaceType);
        UserResponseDto user = new UserResponseDto(1L, "+420123456789", "John Doe");

        return new ReservationResponseDto(
                1L,
                court,
                user,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                false,
                600.0
        );
    }
}
