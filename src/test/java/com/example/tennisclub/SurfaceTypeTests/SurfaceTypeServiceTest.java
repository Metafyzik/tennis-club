package com.example.tennisclub.SurfaceTypeTests;

import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.surfaceType.SurfaceTypeRepository;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    void create_ShouldSaveAndReturnSurfaceType() {
        when(surfaceTypeRepository.save(surfaceType)).thenReturn(surfaceType);

        SurfaceType result = surfaceTypeService.create(surfaceType);

        assertThat(result).isEqualTo(surfaceType);
        verify(surfaceTypeRepository).save(surfaceType);
    }
}
