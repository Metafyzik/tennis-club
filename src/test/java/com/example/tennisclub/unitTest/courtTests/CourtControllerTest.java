package com.example.tennisclub.unitTest.courtTests;

import com.example.tennisclub.auth.security.JwtUtil;
import com.example.tennisclub.auth.security.SecurityConfig;
import com.example.tennisclub.court.CourtController;
import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.dto.CourtRequestDto;
import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import com.example.tennisclub.user.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtController.class)
@Import(SecurityConfig.class)
class CourtControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CourtService courtService;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private CourtResponseDto courtResponseDto;
    private CourtRequestDto validCourtRequestDto;
    private SurfaceTypeResponseDto surfaceTypeResponseDto;

    @BeforeEach
    void setUp() {
        surfaceTypeResponseDto = new SurfaceTypeResponseDto(1L, "Clay", 25.00);
        courtResponseDto = new CourtResponseDto(1L, "Court 1", surfaceTypeResponseDto);
        validCourtRequestDto = new CourtRequestDto("Court 1", 1L);
    }

    @Nested
    class GetAllCourtsTests {

        @Test
        @WithMockUser(roles = "MEMBER")
        void withMemberRole_ShouldReturnCourts() throws Exception {
            when(courtService.getAllCourts()).thenReturn(List.of(courtResponseDto));

            mockMvc.perform(get("/api/courts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Court 1"));

            verify(courtService).getAllCourts();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void withAdminRole_ShouldReturnCourts() throws Exception {
            when(courtService.getAllCourts()).thenReturn(List.of(courtResponseDto));

            mockMvc.perform(get("/api/courts"))
                    .andExpect(status().isOk());

            verify(courtService).getAllCourts();
        }

        @Test
        void withoutAuthentication_ShouldReturn401() throws Exception {
            mockMvc.perform(get("/api/courts"))
                    .andExpect(status().isUnauthorized());

            verify(courtService, never()).getAllCourts();
        }
    }

    @Nested
    class GetCourtTests {

        @Test
        @WithMockUser(roles = "MEMBER")
        void withValidId_ShouldReturnCourt() throws Exception {
            when(courtService.getCourt(1L)).thenReturn(courtResponseDto);

            mockMvc.perform(get("/api/courts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Court 1"));

            verify(courtService).getCourt(1L);
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void withNonExistentId_ShouldReturn404() throws Exception {
            when(courtService.getCourt(999L)).thenThrow(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Court with ID 999 not found"));

            mockMvc.perform(get("/api/courts/999"))
                    .andExpect(status().isNotFound());

            verify(courtService).getCourt(999L);
        }
    }

    @Nested
    class CreateCourtTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void withValidData_ShouldReturnCreatedCourt() throws Exception {
            when(courtService.create(any())).thenReturn(courtResponseDto);

            mockMvc.perform(post("/api/courts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCourtRequestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Court 1"));

            verify(courtService).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void withBlankName_ShouldReturn400() throws Exception {
            var invalidDto = new CourtRequestDto("", 1L);

            mockMvc.perform(post("/api/courts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.name").value("Court name must not be blank"));

            verify(courtService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void withNullSurfaceTypeId_ShouldReturn400() throws Exception {
            var invalidDto = new CourtRequestDto("Court 1", null);

            mockMvc.perform(post("/api/courts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(courtService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void withZeroSurfaceTypeId_ShouldReturn400() throws Exception {
            var invalidDto = new CourtRequestDto("Court 1", 0L);

            mockMvc.perform(post("/api/courts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(courtService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void withMemberRole_ShouldReturn403() throws Exception {
            mockMvc.perform(post("/api/courts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCourtRequestDto)))
                    .andExpect(status().isForbidden());

            verify(courtService, never()).create(any());
        }
    }

    @Nested
    class UpdateCourtTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void withValidData_ShouldReturnUpdatedCourt() throws Exception {
            when(courtService.update(eq(1L), any())).thenReturn(courtResponseDto);

            mockMvc.perform(put("/api/courts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCourtRequestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Court 1"));

            verify(courtService).update(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void withInvalidData_ShouldReturn400() throws Exception {
            var invalidDto = new CourtRequestDto("", 0L);

            mockMvc.perform(put("/api/courts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(courtService, never()).update(any(), any());
        }
    }

    @Nested
    class SoftDeleteCourtTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void withValidId_ShouldReturnSuccessMessage() throws Exception {
            doNothing().when(courtService).softDelete(1L);

            mockMvc.perform(delete("/api/courts/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Court with id 1 deleted successfully."));

            verify(courtService).softDelete(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void withNonExistentId_ShouldReturn404() throws Exception {
            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Court with ID 999 not found"))
                    .when(courtService).softDelete(999L);

            mockMvc.perform(delete("/api/courts/999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(courtService).softDelete(999L);
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void withMemberRole_ShouldReturn403() throws Exception {
            mockMvc.perform(delete("/api/courts/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(courtService, never()).softDelete(any());
        }
    }
}