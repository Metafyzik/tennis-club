package com.example.tennisclub.surfaceType;


import com.example.tennisclub.surfaceType.entity.SurfaceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SurfaceTypeRepository {
    @PersistenceContext
    private EntityManager em;

    public List<SurfaceType> findAll() {
        return em.createQuery("SELECT s FROM SurfaceType s WHERE s.deleted = false", SurfaceType.class)
                .getResultList();
    }

    public Optional<SurfaceType> findById(Long id) {
        SurfaceType st = em.find(SurfaceType.class, id);
        return (st != null && !st.getDeleted()) ? Optional.of(st) : Optional.empty();
    }

    public SurfaceType save(SurfaceType surfaceType) {
        em.persist(surfaceType);
        return surfaceType;
    }

    public SurfaceType update(SurfaceType surfaceType) {
        return em.merge(surfaceType);
    }

    public boolean softDelete(Long id) {
        SurfaceType st = em.find(SurfaceType.class, id);

        if (st == null || st.getDeleted()) return false;
        st.setDeleted(true);
        return true;
    }

    public long count() {
        return em.createQuery("SELECT COUNT(s) FROM SurfaceType s WHERE s.deleted = false", Long.class)
                .getSingleResult();
    }
}


