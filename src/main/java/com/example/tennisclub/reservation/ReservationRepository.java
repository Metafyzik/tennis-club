package com.example.tennisclub.reservation;


import com.example.tennisclub.reservation.entity.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<Reservation> findById(Long id) {
        Reservation r = em.find(Reservation.class, id);
        return (r != null && !r.getDeleted()) ? Optional.of(r) : Optional.empty();
    }

    public List<Reservation> findAllByCourtId(Long courtId) {
        return em.createQuery("""
                SELECT r FROM Reservation r
                WHERE r.deleted = false AND r.court.id = :courtId
                ORDER BY r.startTime ASC
                """, Reservation.class)
                .setParameter("courtId", courtId)
                .getResultList();
    }

    public List<Reservation> findByPhoneNumber(String phoneNumber, boolean futureOnly) {
        String jpql = """
                SELECT r FROM Reservation r
                WHERE r.deleted = false AND r.user.phoneNumber = :phone
                """ + (futureOnly ? " AND r.startTime > CURRENT_TIMESTAMP" : "") + " ORDER BY r.startTime ASC";

        return em.createQuery(jpql, Reservation.class)
                .setParameter("phone", phoneNumber)
                .getResultList();
    }

    public List<Reservation> findOverlappingReservations(Long courtId, LocalDateTime from, LocalDateTime to) {
        return em.createQuery("""
                SELECT r FROM Reservation r
                WHERE r.deleted = false AND r.court.id = :courtId
                AND r.startTime < :to AND r.endTime > :from
                """, Reservation.class)
                .setParameter("courtId", courtId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<Reservation> findAll() {
        return em.createQuery("""
                SELECT r FROM Reservation r
                WHERE r.deleted = false
                ORDER BY r.startTime ASC
                """, Reservation.class)
                .getResultList();
    }

    public Reservation save(Reservation reservation) {
        em.persist(reservation);
        return reservation;
    }

    public Reservation update(Reservation reservation) {
        return em.merge(reservation);
    }

    public boolean softDelete(Long id) {
        Reservation r = em.find(Reservation.class, id);

        if (r == null || r.getDeleted()) return false;
        r.setDeleted(true);
        return true;
    }

    public List<Reservation> findByUsername(String username, boolean futureOnly) {
        String jpql = """
                SELECT r FROM Reservation r
                WHERE r.deleted = false AND r.user.username = :username
                """ + (futureOnly ? " AND r.startTime > CURRENT_TIMESTAMP" : "") + " ORDER BY r.startTime ASC";

        return em.createQuery(jpql, Reservation.class)
                .setParameter("username", username)
                .getResultList();
    }
}


