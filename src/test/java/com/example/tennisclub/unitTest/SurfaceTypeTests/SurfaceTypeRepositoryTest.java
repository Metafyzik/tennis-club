package com.example.tennisclub.unitTest.SurfaceTypeTests;

import com.example.tennisclub.surfaceType.SurfaceTypeRepository;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    void findAll_ShouldReturnOnlyActiveSurfaceTypes() {
        TypedQuery<SurfaceType> mockQuery = mock(TypedQuery.class);
        List<SurfaceType> expectedSurfaceTypes = Arrays.asList(activeSurfaceType);

        when(entityManager.createQuery("SELECT s FROM SurfaceType s WHERE s.deleted = false", SurfaceType.class))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedSurfaceTypes);

        List<SurfaceType> result = surfaceTypeRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result).contains(activeSurfaceType);
        assertThat(result).doesNotContain(deletedSurfaceType);
        verify(entityManager).createQuery("SELECT s FROM SurfaceType s WHERE s.deleted = false", SurfaceType.class);
        verify(mockQuery).getResultList();
    }

    @Test
    void findAll_WithNoActiveSurfaceTypes_ShouldReturnEmptyList() {
        TypedQuery<SurfaceType> mockQuery = mock(TypedQuery.class);
        List<SurfaceType> emptySurfaceTypes = Collections.emptyList();

        when(entityManager.createQuery("SELECT s FROM SurfaceType s WHERE s.deleted = false", SurfaceType.class))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(emptySurfaceTypes);

        List<SurfaceType> result = surfaceTypeRepository.findAll();

        assertThat(result).isEmpty();
        verify(entityManager).createQuery("SELECT s FROM SurfaceType s WHERE s.deleted = false", SurfaceType.class);
        verify(mockQuery).getResultList();
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
    @Test
    void softDelete_WithValidActiveSurfaceTypeId_ShouldReturnTrue() {
        when(entityManager.find(SurfaceType.class, 1L)).thenReturn(activeSurfaceType);

        boolean result = surfaceTypeRepository.softDelete(1L);

        assertThat(result).isTrue();
        assertThat(activeSurfaceType.getDeleted()).isTrue();
        verify(entityManager).find(SurfaceType.class, 1L);
    }

    @Test
    void softDelete_WithAlreadyDeletedSurfaceType_ShouldReturnFalse() {
        when(entityManager.find(SurfaceType.class, 2L)).thenReturn(deletedSurfaceType);

        boolean result = surfaceTypeRepository.softDelete(2L);

        assertThat(result).isFalse();
        verify(entityManager).find(SurfaceType.class, 2L);
    }

    @Test
    void softDelete_WithNonExistentId_ShouldReturnFalse() {
        when(entityManager.find(SurfaceType.class, 999L)).thenReturn(null);

        boolean result = surfaceTypeRepository.softDelete(999L);

        assertThat(result).isFalse();
        verify(entityManager).find(SurfaceType.class, 999L);
    }

    @Test
    void count_ShouldReturnCountOfActiveSurfaceTypes() {
        TypedQuery<Long> mockQuery = mock(TypedQuery.class);
        Long expectedCount = 5L;

        when(entityManager.createQuery("SELECT COUNT(s) FROM SurfaceType s WHERE s.deleted = false", Long.class))
                .thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(expectedCount);

        long result = surfaceTypeRepository.count();

        assertThat(result).isEqualTo(expectedCount);
        verify(entityManager).createQuery("SELECT COUNT(s) FROM SurfaceType s WHERE s.deleted = false", Long.class);
        verify(mockQuery).getSingleResult();
    }

    @Test
    void update_ShouldMergeAndReturnUpdatedSurfaceType() {
        SurfaceType updatedSurfaceType = SurfaceType.builder()
                .id(1L)
                .name("Updated Clay")
                .pricePerMinute(40.0)
                .deleted(false)
                .build();

        when(entityManager.merge(activeSurfaceType)).thenReturn(updatedSurfaceType);

        SurfaceType result = surfaceTypeRepository.update(activeSurfaceType);

        assertThat(result).isEqualTo(updatedSurfaceType);
        assertThat(result.getName()).isEqualTo("Updated Clay");
        assertThat(result.getPricePerMinute()).isEqualTo(40.0);
        verify(entityManager).merge(activeSurfaceType);
    }

}
