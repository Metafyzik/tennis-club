package com.example.tennisclub.auth.refreshToken;

import com.example.tennisclub.auth.refreshToken.entity.RefreshToken;
import com.example.tennisclub.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<RefreshToken> findByToken(String tokenValue) {
        List<RefreshToken> results = em.createQuery("""
                SELECT r FROM RefreshToken r
                WHERE r.token = :token
                """, RefreshToken.class)
                .setParameter("token", tokenValue)
                .getResultList();

        return results.stream().findFirst();
    }
    public void deleteByUser(User user) {
        em.createQuery("""
                DELETE FROM RefreshToken r
                WHERE r.user = :user
                """)
                .setParameter("user", user)
                .executeUpdate();
    }
    public void deleteByToken(String tokenValue) {
        em.createQuery("""
                DELETE FROM RefreshToken r
                WHERE r.token = :token
                """)
                .setParameter("token", tokenValue)
                .executeUpdate();
    }

    public RefreshToken save(RefreshToken refreshToken) {
        em.persist(refreshToken);
        return refreshToken;
    }
}
