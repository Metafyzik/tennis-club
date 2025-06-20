package com.example.tennisclub.user;

import com.example.tennisclub.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    @PersistenceContext
    private EntityManager em;

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        try {
        User user = em.createQuery("""
            SELECT u FROM User u
            WHERE u.phoneNumber = :phone AND u.deleted = false
            """, User.class)
                .setParameter("phone", phoneNumber)
                .getSingleResult();

            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = em.createQuery("""
            SELECT u FROM User u
            WHERE u.username = :username AND u.deleted = false
            """, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    //These functions retrieve also soft deleted users, to make registering users with used phone number or username impossible.
    public Optional<User> findAnyByPhoneNumber(String phoneNumber) {
        try {
            User user = em.createQuery("""
            SELECT u FROM User u
            WHERE u.phoneNumber = :phone
            """, User.class)
                    .setParameter("phone", phoneNumber)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findAnyByUsername(String username) {
        try {
            User user = em.createQuery("""
            SELECT u FROM User u
            WHERE u.username = :username
            """, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

