package com.example.tennisclub.unitTest.SurfaceTypeTests;

import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.surfaceType.SurfaceTypeRepository;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeRequestDTO;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SurfaceTypeServiceTest {
    @Mock
    private SurfaceTypeRepository surfaceTypeRepository;

    @Mock
    private EntityFinder entityFinder;

    @InjectMocks
    private SurfaceTypeService surfaceTypeService;

    private SurfaceType surfaceType;

    @BeforeEach
    void setUp() {
        surfaceType = SurfaceType.builder()
                .id(1L)
                .name("Clay")
                .pricePerMinute(30.0)
                .deleted(false)
                .build();
    }

    @Test
    void findByIdOrThrow_WithValidId_ShouldReturnSurfaceType() {
        when(surfaceTypeRepository.findById(1L)).thenReturn(Optional.of(surfaceType));
        when(entityFinder.findByIdOrThrow(Optional.of(surfaceType), 1L, "SurfaceType"))
                .thenReturn(surfaceType);

        SurfaceType result = surfaceTypeService.findByIdOrThrow(1L);

        assertThat(result).isEqualTo(surfaceType);
        verify(surfaceTypeRepository).findById(1L);
        verify(entityFinder).findByIdOrThrow(Optional.of(surfaceType), 1L, "SurfaceType");
    }

    @Test
    @DisplayName("Should throw exception when surface type does not exist")
    void findByIdOrThrow_WhenSurfaceTypeDoesNotExist_ShouldThrowException() {
        // Arrange
        Long id = 996L;
        Optional<SurfaceType> emptyOptional = Optional.empty();
        ResponseStatusException expectedException = new ResponseStatusException(
                HttpStatus.NOT_FOUND, "SurfaceType with ID " + id + " not found");

        when(surfaceTypeRepository.findById(id)).thenReturn(emptyOptional);
        when(entityFinder.findByIdOrThrow(emptyOptional, id, "SurfaceType"))
                .thenThrow(expectedException);

        // Act & Assert
        assertThatThrownBy(() -> surfaceTypeService.findByIdOrThrow(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("404 NOT_FOUND \"SurfaceType with ID " + id + " not found\"");

        verify(surfaceTypeRepository).findById(id);
        verify(entityFinder).findByIdOrThrow(emptyOptional, id, "SurfaceType");
    }

    @Test  
    void getAll_ShouldReturnAllSurfaceTypes(){
        when(surfaceTypeRepository.findAll()).thenReturn(List.of(surfaceType));

        List<SurfaceTypeResponseDto> result = surfaceTypeService.getAll();

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().name()).isEqualTo(surfaceType.getName());
    }

    @Test
    void save_ShouldSaveAndReturnSurfaceType() {
        when(surfaceTypeRepository.save(surfaceType)).thenReturn(surfaceType);

        SurfaceType result = surfaceTypeService.save(surfaceType);

        assertThat(result).isEqualTo(surfaceType);
        verify(surfaceTypeRepository).save(surfaceType);
    }

    @Test
    void create_ShouldCreateNewSurfaceType(){
        SurfaceTypeRequestDTO surfaceTypeRequestDTO = new SurfaceTypeRequestDTO(surfaceType.getName(),surfaceType.getPricePerMinute());

        when(surfaceTypeRepository.save(any(SurfaceType.class))).thenReturn(surfaceType);

        SurfaceTypeResponseDto result = surfaceTypeService.create(surfaceTypeRequestDTO);

        assertThat(result.name()).isEqualTo(surfaceTypeRequestDTO.name());
        assertThat(result.pricePerMinute()).isEqualTo(surfaceTypeRequestDTO.pricePerMinute());
        verify(surfaceTypeRepository).save(any(SurfaceType.class));
    }

    @Test
    void count_ShouldReturnNumberOfExistingSurfaceTypes(){
        when(surfaceTypeRepository.count()).thenReturn(2L);

        Long numberOfSurfaceTypes = surfaceTypeService.count();

        assertThat(numberOfSurfaceTypes).isEqualTo(2L);

        verify(surfaceTypeRepository).count();
    }

    @Test
    void softDelete_ShouldDeleteExistingSurfaceType(){
        when(surfaceTypeRepository.softDelete(1l)).thenReturn(true);

        assertThatCode(() -> surfaceTypeService.softDelete(1L))
                .doesNotThrowAnyException();

        verify(surfaceTypeRepository).softDelete(1L);
    }

    @Test
    void softDelete_WithNonExistentId_ShouldThrowException() {
        when(surfaceTypeRepository.softDelete(999L)).thenReturn(false);

        assertThatThrownBy(() -> surfaceTypeService.softDelete(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("SurfaceType with id 999 not found");

        verify(surfaceTypeRepository).softDelete(999L);
    }

    @Test
    void mapToResponseDto_ShouldReturnResponseDTO() {
        SurfaceTypeResponseDto SurfaceTypeResponseDto = new SurfaceTypeResponseDto(surfaceType.getId(),surfaceType.getName(),surfaceType.getPricePerMinute());

        SurfaceTypeResponseDto result = surfaceTypeService.mapToResponseDto(surfaceType);

        assertThat(result.name()).isEqualTo(SurfaceTypeResponseDto.name());
        assertThat(result.pricePerMinute()).isEqualTo(SurfaceTypeResponseDto.pricePerMinute());
        assertThat(result.id()).isEqualTo(SurfaceTypeResponseDto.id());
    }

    @Test
    void getSurfaceTypeById_ShouldSurfaceTypeResponseDto() {
        SurfaceTypeResponseDto SurfaceTypeResponseDto = new SurfaceTypeResponseDto(surfaceType.getId(),surfaceType.getName(),surfaceType.getPricePerMinute());

        when(surfaceTypeRepository.findById(1L)).thenReturn(Optional.of(surfaceType));
        when(entityFinder.findByIdOrThrow(Optional.of(surfaceType), 1L, "SurfaceType"))
                .thenReturn(surfaceType);

        SurfaceTypeResponseDto result = surfaceTypeService.getSurfaceTypeById(1l);

        assertThat(result.name()).isEqualTo(SurfaceTypeResponseDto.name());
        assertThat(result.pricePerMinute()).isEqualTo(SurfaceTypeResponseDto.pricePerMinute());
        assertThat(result.id()).isEqualTo(SurfaceTypeResponseDto.id());

        verify(surfaceTypeRepository).findById(1L);
        verify(entityFinder).findByIdOrThrow(Optional.of(surfaceType), 1L, "SurfaceType");

    }

    @Test
    void mapToEntity_ShouldReturnEntity(){
        SurfaceTypeRequestDTO surfaceTypeRequestDTO = new SurfaceTypeRequestDTO(surfaceType.getName(),surfaceType.getPricePerMinute());

        SurfaceType result = surfaceTypeService.mapToEntity(surfaceTypeRequestDTO);

        assertThat(result.getName()).isEqualTo(surfaceTypeRequestDTO.name());
        assertThat(result.getPricePerMinute()).isEqualTo(surfaceTypeRequestDTO.pricePerMinute());
    }

    @Test
    void update_ShouldUpdateExistingSurfaceType() {
        SurfaceTypeRequestDTO surfaceTypeRequestDTO = new SurfaceTypeRequestDTO("updated surfacetype", 2 * surfaceType.getPricePerMinute());
        SurfaceType updateSurfaceType = SurfaceType.builder()
                .id(1L)
                .name("updated surfacetype")
                .pricePerMinute(2 * surfaceType.getPricePerMinute())
                .build();

        when(surfaceTypeRepository.findById(1l)).thenReturn(Optional.of(surfaceType));
        when(entityFinder.findByIdOrThrow(Optional.of(surfaceType), 1L, "SurfaceType")).thenReturn(surfaceType);
        when(surfaceTypeRepository.update(any(SurfaceType.class))).thenReturn(updateSurfaceType);

        SurfaceTypeResponseDto result = surfaceTypeService.update(1L, surfaceTypeRequestDTO);

        assertThat(result.name()).isEqualTo("updated surfacetype");

        verify(surfaceTypeRepository).update(any(SurfaceType.class));
    }

    @Test
    void update_ShouldThrowWhenSurfaceTypeDoesNotExist(){
        when(surfaceTypeService.findByIdOrThrow(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "SurfaceType with ID 999 not found"));

        SurfaceTypeRequestDTO surfaceTypeRequestDTO = new SurfaceTypeRequestDTO("updated surfacetype", 2 * surfaceType.getPricePerMinute());

        assertThatThrownBy(() -> surfaceTypeService.update(999L, surfaceTypeRequestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("SurfaceType with ID 999 not found");

        verify(surfaceTypeRepository, never()).update(any());
    }
}
