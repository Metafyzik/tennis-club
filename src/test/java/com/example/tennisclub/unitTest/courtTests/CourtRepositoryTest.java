package com.example.tennisclub.unitTest.courtTests;

import com.example.tennisclub.court.CourtRepository;
import com.example.tennisclub.court.entity.Court;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Court> typedQuery;

    @InjectMocks
    private CourtRepository courtRepository;
    private Court activeCourt;
    private Court deletedCourt;
    private SurfaceType surfaceType;

    @BeforeEach
    void setUp() {
        surfaceType = SurfaceType.builder()
                .id(1L)
                .name("Clay")
                .pricePerMinute(25.00)
                .deleted(false)
                .build();

        activeCourt = Court.builder()
                .id(1L)
                .name("Court 1")
                .surfaceType(surfaceType)
                .deleted(false)
                .build();

        deletedCourt = Court.builder()
                .id(2L)
                .name("Court 2")
                .surfaceType(surfaceType)
                .deleted(true)
                .build();
    }

    @Test
    void findAll_ShouldReturnOnlyActiveCourts() {
        List<Court> expectedCourts = Arrays.asList(activeCourt);
        when(entityManager.createQuery("SELECT c FROM Court c WHERE c.deleted = false", Court.class))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedCourts);

        List<Court> result = courtRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeleted()).isFalse();
        assertThat(result.get(0).getName()).isEqualTo("Court 1");
        verify(entityManager).createQuery("SELECT c FROM Court c WHERE c.deleted = false", Court.class);
        verify(typedQuery).getResultList();
    }

    @Test
    void findAll_WithNoActiveCourts_ShouldReturnEmptyList() {
        when(entityManager.createQuery("SELECT c FROM Court c WHERE c.deleted = false", Court.class))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());

        List<Court> result = courtRepository.findAll();

        assertThat(result).isEmpty();
        verify(entityManager).createQuery("SELECT c FROM Court c WHERE c.deleted = false", Court.class);
        verify(typedQuery).getResultList();
    }

    @Test
    void findById_WithValidActiveCourtId_ShouldReturnCourt() {
        when(entityManager.find(Court.class, 1L)).thenReturn(activeCourt);

        Optional<Court> result = courtRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getDeleted()).isFalse();
        verify(entityManager).find(Court.class, 1L);
    }

    @Test
    void findById_WithDeletedCourtId_ShouldReturnEmpty() {
        when(entityManager.find(Court.class, 2L)).thenReturn(deletedCourt);

        Optional<Court> result = courtRepository.findById(2L);

        assertThat(result).isEmpty();
        verify(entityManager).find(Court.class, 2L);
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        when(entityManager.find(Court.class, 999L)).thenReturn(null);

        Optional<Court> result = courtRepository.findById(999L);

        assertThat(result).isEmpty();
        verify(entityManager).find(Court.class, 999L);
    }

    @Test
    void save_ShouldPersistCourt() {
        doNothing().when(entityManager).persist(activeCourt);

        Court result = courtRepository.save(activeCourt);

        assertThat(result).isEqualTo(activeCourt);
        verify(entityManager).persist(activeCourt);
    }

    @Test
    void update_ShouldMergeCourt() {
        when(entityManager.merge(activeCourt)).thenReturn(activeCourt);

        Court result = courtRepository.update(activeCourt);

        assertThat(result).isEqualTo(activeCourt);
        verify(entityManager).merge(activeCourt);
    }

    @Test
    void softDelete_WithValidActiveCourtId_ShouldReturnTrue() {
        when(entityManager.find(Court.class, 1L)).thenReturn(activeCourt);

        boolean result = courtRepository.softDelete(1L);

        assertThat(result).isTrue();
        assertThat(activeCourt.getDeleted()).isTrue();
        verify(entityManager).find(Court.class, 1L);
    }

    @Test
    void softDelete_WithAlreadyDeletedCourt_ShouldReturnFalse() {
        when(entityManager.find(Court.class, 2L)).thenReturn(deletedCourt);

        boolean result = courtRepository.softDelete(2L);

        assertThat(result).isFalse();
        verify(entityManager).find(Court.class, 2L);
    }

    @Test
    void softDelete_WithNonExistentId_ShouldReturnFalse() {
        when(entityManager.find(Court.class, 999L)).thenReturn(null);

        boolean result = courtRepository.softDelete(999L);

        assertThat(result).isFalse();
        verify(entityManager).find(Court.class, 999L);
    }
}