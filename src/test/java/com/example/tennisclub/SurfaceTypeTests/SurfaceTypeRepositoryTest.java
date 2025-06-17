package com.example.tennisclub.SurfaceTypeTests;

import com.example.tennisclub.surfaceType.entity.SurfaceType;
import com.example.tennisclub.surfaceType.SurfaceTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurfaceTypeRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SurfaceTypeRepository surfaceTypeRepository;

    private SurfaceType activeSurfaceType;
    private SurfaceType deletedSurfaceType;

    @BeforeEach
    void setUp() {
        activeSurfaceType = SurfaceType.builder()
                .id(1L)
                .name("Clay")
                .pricePerMinute(30.0)
                .deleted(false)
                .build();

        deletedSurfaceType = SurfaceType.builder()
                .id(2L)
                .name("Grass")
                .pricePerMinute(35.0)
                .deleted(true)
                .build();
    }

    @Test
    void findById_WithValidActiveSurfaceType_ShouldReturnSurfaceType() {
        when(entityManager.find(SurfaceType.class, 1L)).thenReturn(activeSurfaceType);

        Optional<SurfaceType> result = surfaceTypeRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(activeSurfaceType);
        verify(entityManager).find(SurfaceType.class, 1L);
    }

    @Test
    void findById_WithDeletedSurfaceType_ShouldReturnEmpty() {
        when(entityManager.find(SurfaceType.class, 2L)).thenReturn(deletedSurfaceType);

        Optional<SurfaceType> result = surfaceTypeRepository.findById(2L);

        assertThat(result).isEmpty();
        verify(entityManager).find(SurfaceType.class, 2L);
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        when(entityManager.find(SurfaceType.class, 999L)).thenReturn(null);

        Optional<SurfaceType> result = surfaceTypeRepository.findById(999L);

        assertThat(result).isEmpty();
        verify(entityManager).find(SurfaceType.class, 999L);
    }

    @Test
    void save_ShouldPersistSurfaceType() {
        doNothing().when(entityManager).persist(activeSurfaceType);

        SurfaceType result = surfaceTypeRepository.save(activeSurfaceType);

        assertThat(result).isEqualTo(activeSurfaceType);
        verify(entityManager).persist(activeSurfaceType);
    }
}
