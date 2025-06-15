package com.example.tennisclub.court;


import com.example.tennisclub.court.entity.Court;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public class CourtRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Court> findAll() {
        return em.createQuery("SELECT c FROM Court c WHERE c.deleted = false", Court.class)
                .getResultList();
    }

    public Optional<Court> findById(Long id) {
        Court court = em.find(Court.class, id);
        return (court != null && !court.getDeleted()) ? Optional.of(court) : Optional.empty();
    }

    public Court save(Court court) {
        em.persist(court);
        return court;
    }

    public Court update(Court court) {
        return em.merge(court);
    }

    public boolean softDelete(Long id) {
        Court court = em.find(Court.class, id);
        if (court == null || court.getDeleted()) return false;
        court.setDeleted(true);
        return true;
    }
}
