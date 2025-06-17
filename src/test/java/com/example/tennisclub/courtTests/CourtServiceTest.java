package com.example.tennisclub.courtTests;

import com.example.tennisclub.court.CourtRepository;
import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.dto.CourtRequestDto;
import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private SurfaceTypeService surfaceTypeService;

    @Mock
    private EntityFinder entityFinder;

    @InjectMocks
    private CourtService courtService;

    private Court court;
    private SurfaceType surfaceType;
    private CourtRequestDto courtRequestDto;

    private static final long VALID_SURFACE_TYPE_ID = 1L;
    private static final long NON_EXISTENT_ID = 999L;

    @BeforeEach
    void setUp() {
        surfaceType = SurfaceType.builder()
                .id(VALID_SURFACE_TYPE_ID)
                .name("Clay")
                .pricePerMinute(25.00)
                .deleted(false)
                .build();

        court = Court.builder()
                .id(VALID_SURFACE_TYPE_ID)
                .name("Court 1")
                .surfaceType(surfaceType)
                .deleted(false)
                .build();

        courtRequestDto = new CourtRequestDto("Court 1", VALID_SURFACE_TYPE_ID);
    }
    @Nested
    class GetAllCourtsTests {

        @Test
        void getAllCourts_ShouldReturnAllCourts() {
            List<Court> courts = Arrays.asList(court);
            when(courtRepository.findAll()).thenReturn(courts);

            List<CourtResponseDto> result = courtService.getAllCourts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Court 1");
            assertThat(result.get(0).surfaceType().name()).isEqualTo("Clay");
            verify(courtRepository).findAll();
        }

        @Test
        void getAllCourts_WithEmptyRepository_ShouldReturnEmptyList() {
            when(courtRepository.findAll()).thenReturn(Arrays.asList());

            List<CourtResponseDto> result = courtService.getAllCourts();

            assertThat(result).isEmpty();
            verify(courtRepository).findAll();
        }
    }

    @Nested
    class SaveCourtTests{

        @Test
        void save_ShouldSaveAndReturnCourt() {
            when(courtRepository.save(court)).thenReturn(court);

            Court result = courtService.save(court);

            assertThat(result).isEqualTo(court);
            verify(courtRepository).save(court);
        }
    }

    @Nested
    class findCourtEntityByIdOrThrowTests {

        @Test
        void findCourtEntityByIdOrThrow_WithValidId_ShouldReturnCourt() {
            when(courtRepository.findById(VALID_SURFACE_TYPE_ID)).thenReturn(Optional.of(court));
            when(entityFinder.findByIdOrThrow(Optional.of(court), VALID_SURFACE_TYPE_ID, "Court")).thenReturn(court);

            Court result = courtService.findCourtEntityByIdOrThrow(VALID_SURFACE_TYPE_ID);

            assertThat(result).isEqualTo(court);
            verify(courtRepository).findById(VALID_SURFACE_TYPE_ID);
            verify(entityFinder).findByIdOrThrow(Optional.of(court), VALID_SURFACE_TYPE_ID, "Court");
        }

        @Test
        void findCourtEntityByIdOrThrow_WithNonExistentId_ShouldThrowException() {
            when(courtRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
            when(entityFinder.findByIdOrThrow(Optional.empty(), NON_EXISTENT_ID, "Court"))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Court with ID 999 not found"));

            assertThatThrownBy(() -> courtService.findCourtEntityByIdOrThrow(NON_EXISTENT_ID))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Court with ID 999 not found");

            verify(courtRepository).findById(NON_EXISTENT_ID);
            verify(entityFinder).findByIdOrThrow(Optional.empty(), NON_EXISTENT_ID, "Court");
        }
    }

    @Nested
    class CreateCourtTests {

        @Test
        void create_WithValidData_ShouldCreateAndReturnCourt() {
            when(surfaceTypeService.findByIdOrThrow(VALID_SURFACE_TYPE_ID)).thenReturn(surfaceType);
            when(courtRepository.save(any(Court.class))).thenReturn(court);

            CourtResponseDto result = courtService.create(courtRequestDto);

            assertThat(result.name()).isEqualTo("Court 1");
            assertThat(result.surfaceType().name()).isEqualTo("Clay");
            verify(surfaceTypeService).findByIdOrThrow(VALID_SURFACE_TYPE_ID);
            verify(courtRepository).save(any(Court.class));
        }

        @Test
        void create_WithNonExistentSurfaceType_ShouldThrowException() {
            when(surfaceTypeService.findByIdOrThrow(NON_EXISTENT_ID))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "SurfaceType with ID 999 not found"));

            CourtRequestDto invalidDto = new CourtRequestDto("Court 1", NON_EXISTENT_ID);

            assertThatThrownBy(() -> courtService.create(invalidDto))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("SurfaceType with ID 999 not found");

            verify(surfaceTypeService).findByIdOrThrow(NON_EXISTENT_ID);
            verify(courtRepository, never()).save(any());
        }
    }


    @Nested
    class UpdateCourtTests {

        @Test
        void update_WithValidData_ShouldUpdateAndReturnCourt() {
            CourtRequestDto updateDto = new CourtRequestDto("Updated Court", VALID_SURFACE_TYPE_ID);
            Court updatedCourt = Court.builder()
                    .id(VALID_SURFACE_TYPE_ID)
                    .name("Updated Court")
                    .surfaceType(surfaceType)
                    .deleted(false)
                    .build();

            when(surfaceTypeService.findByIdOrThrow(VALID_SURFACE_TYPE_ID)).thenReturn(surfaceType);
            when(courtRepository.findById(VALID_SURFACE_TYPE_ID)).thenReturn(Optional.of(court));
            when(entityFinder.findByIdOrThrow(Optional.of(court), VALID_SURFACE_TYPE_ID, "Court")).thenReturn(court);
            when(courtRepository.update(any(Court.class))).thenReturn(updatedCourt);

            CourtResponseDto result = courtService.update(VALID_SURFACE_TYPE_ID, updateDto);

            assertThat(result.name()).isEqualTo("Updated Court");
            verify(surfaceTypeService).findByIdOrThrow(VALID_SURFACE_TYPE_ID);
            verify(courtRepository).update(any(Court.class));
        }

        @Test
        void update_WithNonExistentCourt_ShouldThrowException() {
            when(courtRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
            when(entityFinder.findByIdOrThrow(Optional.empty(), NON_EXISTENT_ID, "Court"))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Court with ID 999 not found"));

            assertThatThrownBy(() -> courtService.update(NON_EXISTENT_ID, courtRequestDto))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Court with ID 999 not found");

            verify(courtRepository, never()).update(any());
        }

        @Test
        void update_WithNonExistentSurfaceType_ShouldThrowException() {
            when(surfaceTypeService.findByIdOrThrow(NON_EXISTENT_ID))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "SurfaceType with ID 999 not found"));

            CourtRequestDto invalidDto = new CourtRequestDto("Court 1", NON_EXISTENT_ID);

            assertThatThrownBy(() -> courtService.update(VALID_SURFACE_TYPE_ID, invalidDto))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("SurfaceType with ID 999 not found");

            verify(courtRepository, never()).update(any());
        }
    }

    @Nested
    class SoftDeleteCourtsTests {

        @Test
        void softDelete_WithValidId_ShouldDeleteSuccessfully() {
            when(courtRepository.softDelete(VALID_SURFACE_TYPE_ID)).thenReturn(true);

            assertThatCode(() -> courtService.softDelete(VALID_SURFACE_TYPE_ID))
                    .doesNotThrowAnyException();

            verify(courtRepository).softDelete(VALID_SURFACE_TYPE_ID);
        }

        @Test
        void softDelete_WithNonExistentId_ShouldThrowException() {
            when(courtRepository.softDelete(NON_EXISTENT_ID)).thenReturn(false);

            assertThatThrownBy(() -> courtService.softDelete(NON_EXISTENT_ID))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Court with ID 999 not found");

            verify(courtRepository).softDelete(NON_EXISTENT_ID);
        }
    }

    @Nested
    class MappingCourtTests{

        @Test
        void mapToEntity_ShouldMapDtoToCourt() {
            when(surfaceTypeService.findByIdOrThrow(VALID_SURFACE_TYPE_ID)).thenReturn(surfaceType);

            Court result = courtService.mapToEntity(courtRequestDto);

            assertThat(result.getName()).isEqualTo("Court 1");
            assertThat(result.getSurfaceType()).isEqualTo(surfaceType);
            verify(surfaceTypeService).findByIdOrThrow(VALID_SURFACE_TYPE_ID);
        }

        @Test
        void mapToResponseDto_ShouldMapCourtToDto() {
            CourtResponseDto result = courtService.mapToResponseDto(court);

            assertThat(result.id()).isEqualTo(VALID_SURFACE_TYPE_ID);
            assertThat(result.name()).isEqualTo("Court 1");
            assertThat(result.surfaceType().id()).isEqualTo(VALID_SURFACE_TYPE_ID);
            assertThat(result.surfaceType().name()).isEqualTo("Clay");
            assertThat(result.surfaceType().pricePerMinute()).isEqualTo(25.00);
        }
    }

    @Nested
    class SingleCourtRetrievalTests {

        @Test
        void findAllCourtEntities_ShouldReturnAllCourts() {
            List<Court> courts = List.of(court);
            when(courtRepository.findAll()).thenReturn(courts);

            List<Court> result = courtService.findAllCourtEntities();

            assertThat(result).isEqualTo(courts);
            verify(courtRepository).findAll();
        }

        @Test
        void getCourt_WithValidId_ShouldReturnCourtResponseDto() {
            when(courtRepository.findById(VALID_SURFACE_TYPE_ID)).thenReturn(Optional.of(court));
            when(entityFinder.findByIdOrThrow(Optional.of(court), VALID_SURFACE_TYPE_ID, "Court")).thenReturn(court);

            CourtResponseDto result = courtService.getCourt(VALID_SURFACE_TYPE_ID);

            assertThat(result.id()).isEqualTo(VALID_SURFACE_TYPE_ID);
            assertThat(result.name()).isEqualTo("Court 1");
            assertThat(result.surfaceType().name()).isEqualTo("Clay");
            verify(courtRepository).findById(VALID_SURFACE_TYPE_ID);
        }
    }
}
