package com.example.tennisclub.unitTest.SurfaceTypeTests;

import com.example.tennisclub.auth.security.JwtUtil;
import com.example.tennisclub.auth.security.SecurityConfig;
import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.surfaceType.SurfaceTypeController;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeRequestDTO;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SurfaceTypeController.class)
@Import(SecurityConfig.class)
public class SurfaceTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurfaceTypeService surfaceTypeService;

    @MockitoBean
    private EntityFinder entityFinder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private SurfaceTypeResponseDto surfaceTypeResponseDto;
    private SurfaceTypeRequestDTO surfaceTypeRequestDTO;
    private List<SurfaceTypeResponseDto> surfaceTypeList;

    @BeforeEach
    void setUp() {
        surfaceTypeResponseDto = new SurfaceTypeResponseDto(1L, "Clay", 25.50);

        surfaceTypeRequestDTO = new SurfaceTypeRequestDTO("Clay", 25.50);

        SurfaceTypeResponseDto surfaceType2 = new SurfaceTypeResponseDto(2L, "Grass", 30.00);

        surfaceTypeList = Arrays.asList(surfaceTypeResponseDto, surfaceType2);
    }

    @Nested
    class GetAllSurfaceTypes {
        @Test
        @WithMockUser(roles = "ADMIN")
        void getAll_WithAdminRole_ShouldReturnAllSurfaceTypes() throws Exception {
            when(surfaceTypeService.getAll()).thenReturn(surfaceTypeList);

            mockMvc.perform(get("/api/surface-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Clay")))
                    .andExpect(jsonPath("$[0].pricePerMinute", is(25.50)))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Grass")))
                    .andExpect(jsonPath("$[1].pricePerMinute", is(30.00)));

            verify(surfaceTypeService, times(1)).getAll();
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void getAll_WithMemberRole_ShouldReturnAllSurfaceTypes() throws Exception {
            when(surfaceTypeService.getAll()).thenReturn(surfaceTypeList);

            mockMvc.perform(get("/api/surface-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(surfaceTypeService, times(1)).getAll();
        }

        @Test
        void getAll_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/surface-types"))
                    .andExpect(status().isUnauthorized());

            verify(surfaceTypeService, never()).getAll();
        }
    }

    @Nested
    class GetSurfaceTypeById {

        @Test
        @WithMockUser(roles = "ADMIN")
        void getSurfaceTypeById_WithAdminRole_ShouldReturnSurfaceType() throws Exception {
            when(surfaceTypeService.getSurfaceTypeById(1L)).thenReturn(surfaceTypeResponseDto);

            mockMvc.perform(get("/api/surface-types/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Clay")))
                    .andExpect(jsonPath("$.pricePerMinute", is(25.50)));

            verify(surfaceTypeService, times(1)).getSurfaceTypeById(1L);
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void getSurfaceTypeById_WithMemberRole_ShouldReturnSurfaceType() throws Exception {
            when(surfaceTypeService.getSurfaceTypeById(1L)).thenReturn(surfaceTypeResponseDto);

            mockMvc.perform(get("/api/surface-types/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Clay")));

            verify(surfaceTypeService, times(1)).getSurfaceTypeById(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getSurfaceTypeById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
            when(surfaceTypeService.getSurfaceTypeById(999L))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "SurfaceType with ID 999 not found"));

            mockMvc.perform(get("/api/surface-types/999"))
                    .andExpect(status().isNotFound());

            verify(surfaceTypeService, times(1)).getSurfaceTypeById(999L);
        }

        @Test
        void getSurfaceTypeById_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/surface-types/1"))
                    .andExpect(status().isUnauthorized());

            verify(surfaceTypeService, never()).getSurfaceTypeById(any());
        }
    }

    @Nested
    class CreateSurfaceType {
        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithValidData_ShouldCreateSurfaceType() throws Exception {
            when(surfaceTypeService.create(any(SurfaceTypeRequestDTO.class))).thenReturn(surfaceTypeResponseDto);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(surfaceTypeRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Clay")))
                    .andExpect(jsonPath("$.pricePerMinute", is(25.50)));

            verify(surfaceTypeService, times(1)).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithBlankName_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("", 25.50);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithNullName_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO(null, 25.50);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithNullPricePerMinute_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("Clay", null);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithPricePerMinuteBelowMinimum_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("Clay", 0.005);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithPricePerMinuteZero_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("Clay", 0.0);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithPricePerMinuteAtMinimum_ShouldCreateSurfaceType() throws Exception {
            SurfaceTypeRequestDTO validDto = new SurfaceTypeRequestDTO("Clay", 0.01);
            SurfaceTypeResponseDto responseDto = new SurfaceTypeResponseDto(1L, "Clay", 0.01);
            when(surfaceTypeService.create(any(SurfaceTypeRequestDTO.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pricePerMinute", is(0.01)));

            verify(surfaceTypeService, times(1)).create(any(SurfaceTypeRequestDTO.class));
        }

        @Test
        void create_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(surfaceTypeRequestDTO)))
                    .andExpect(status().isUnauthorized());

            verify(surfaceTypeService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void create_WithMemberRole_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(surfaceTypeRequestDTO)))
                    .andExpect(status().isForbidden());

            verify(surfaceTypeService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void create_WithWhitespaceOnlyName_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("   ", 25.50);

            mockMvc.perform(post("/api/surface-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).create(any());
        }
    }

    @Nested
    class UpdateSurfaceType {
        @Test
        @WithMockUser(roles = "ADMIN")
        void update_WithValidData_ShouldUpdateSurfaceType() throws Exception {
            SurfaceTypeResponseDto updatedResponse = new SurfaceTypeResponseDto(1L, "Hard Court", 35.00);
            SurfaceTypeRequestDTO updateRequest = new SurfaceTypeRequestDTO("Hard Court", 35.00);

            when(surfaceTypeService.update(eq(1L), any(SurfaceTypeRequestDTO.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/surface-types/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Hard Court")))
                    .andExpect(jsonPath("$.pricePerMinute", is(35.00)));

            verify(surfaceTypeService, times(1)).update(eq(1L), any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_WithNonExistentId_ShouldReturnNotFound() throws Exception {
            when(surfaceTypeService.update(eq(999L), any(SurfaceTypeRequestDTO.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "SurfaceType with ID 999 not found"));

            mockMvc.perform(put("/api/surface-types/999")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(surfaceTypeRequestDTO)))
                    .andExpect(status().isNotFound());

            verify(surfaceTypeService, times(1)).update(eq(999L), any(SurfaceTypeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("", null);

            mockMvc.perform(put("/api/surface-types/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).update(any(), any());
        }

        @Test
        void update_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(put("/api/surface-types/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(surfaceTypeRequestDTO)))
                    .andExpect(status().isUnauthorized());

            verify(surfaceTypeService, never()).update(any(), any());
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void update_WithMemberRole_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(put("/api/surface-types/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(surfaceTypeRequestDTO)))
                    .andExpect(status().isForbidden());

            verify(surfaceTypeService, never()).update(any(), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
            mockMvc.perform(put("/api/surface-types/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).update(any(), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void update_WithWhitespaceOnlyName_ShouldReturnBadRequest() throws Exception {
            SurfaceTypeRequestDTO invalidDto = new SurfaceTypeRequestDTO("   ", 25.50);

            mockMvc.perform(put("/api/surface-types/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(surfaceTypeService, never()).update(any(), any());
        }
    }


    @Nested
    class DeleteSurfaceType {
        @Test
        @WithMockUser(roles = "ADMIN")
        void delete_WithValidId_ShouldDeleteSurfaceType() throws Exception {
            doNothing().when(surfaceTypeService).softDelete(1L);

            mockMvc.perform(delete("/api/surface-types/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Surface type with id 1 deleted successfully."));

            verify(surfaceTypeService, times(1)).softDelete(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void delete_WithNonExistentId_ShouldReturnNotFound() throws Exception {
            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "SurfaceType with id 999 not found."))
                    .when(surfaceTypeService).softDelete(999L);

            mockMvc.perform(delete("/api/surface-types/999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(surfaceTypeService, times(1)).softDelete(999L);
        }

        @Test
        void delete_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/surface-types/1")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(surfaceTypeService, never()).softDelete(any());
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void delete_WithMemberRole_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(delete("/api/surface-types/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(surfaceTypeService, never()).softDelete(any());
        }
    }

    @Nested
    class GetSurfaceTypeCount {
        @Test
        @WithMockUser(roles = "ADMIN")
        void count_WithAdminRole_ShouldReturnCount() throws Exception {
            when(surfaceTypeService.count()).thenReturn(5L);

            mockMvc.perform(get("/api/surface-types/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string("5"));

            verify(surfaceTypeService, times(1)).count();
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        void count_WithMemberRole_ShouldReturnCount() throws Exception {
            when(surfaceTypeService.count()).thenReturn(3L);

            mockMvc.perform(get("/api/surface-types/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string("3"));

            verify(surfaceTypeService, times(1)).count();
        }

        @Test
        void count_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/surface-types/count"))
                    .andExpect(status().isUnauthorized());

            verify(surfaceTypeService, never()).count();
        }
    }

}