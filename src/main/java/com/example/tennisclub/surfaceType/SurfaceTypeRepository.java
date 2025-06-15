package com.example.tennisclub.surfaceType;


import com.example.tennisclub.surfaceType.entity.SurfaceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SurfaceTypeRepository {
    @PersistenceContext
    private EntityManager em;

    public Optional<SurfaceType> findById(Long id) {
        SurfaceType st = em.find(SurfaceType.class, id);
        return (st != null && !st.getDeleted()) ? Optional.of(st) : Optional.empty();
    }

    public SurfaceType save(SurfaceType surfaceType) {
        em.persist(surfaceType);
        return surfaceType;
    }
}


