package com.example.tennisclub.unitTest.authTests;

import com.example.tennisclub.auth.refreshToken.RefreshTokenRepository;
import com.example.tennisclub.auth.refreshToken.entity.RefreshToken;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<RefreshToken> typedQuery;

    @Mock
    private Query query;

    @InjectMocks
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private RefreshToken testRefreshToken;

    private static final String FIND_BY_TOKEN_QUERY = """
        SELECT r FROM RefreshToken r
        WHERE r.token = :token
        """;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .username("testuser")
                .password("password123")
                .deleted(false)
                .roles(Set.of(Role.MEMBER))
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("test-refresh-token-123")
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .user(testUser)
                .build();
    }

    @Nested
    class FindByTokenTests {

        @Test
        @DisplayName("Should return refresh token when token exists")
        void whenTokenExists_ShouldReturnRefreshToken() {
            String tokenValue = testRefreshToken.getToken();
            when(entityManager.createQuery(anyString(), eq(RefreshToken.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of(testRefreshToken));

            Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenValue);

            assertThat(result).isPresent().contains(testRefreshToken);
            verify(entityManager).createQuery(FIND_BY_TOKEN_QUERY, RefreshToken.class);
        }

        @Test
        @DisplayName("Should return empty optional when token does not exist")
        void whenTokenDoesNotExist_ShouldReturnEmpty() {
            when(entityManager.createQuery(anyString(), eq(RefreshToken.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("token", "non-existent")).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

            Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return first token if multiple tokens found")
        void whenMultipleTokensFound_ShouldReturnFirst() {
            RefreshToken secondToken = RefreshToken.builder()
                    .id(2L)
                    .token("duplicate")
                    .expiryDate(Instant.now())
                    .user(testUser)
                    .build();

            when(entityManager.createQuery(anyString(), eq(RefreshToken.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("token", "duplicate")).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of(testRefreshToken, secondToken));

            Optional<RefreshToken> result = refreshTokenRepository.findByToken("duplicate");

            assertThat(result).isPresent().contains(testRefreshToken);
        }

        @Test
        @DisplayName("Should handle null token parameter gracefully")
        void whenTokenIsNull_ShouldReturnEmpty() {
            when(entityManager.createQuery(anyString(), eq(RefreshToken.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("token", null)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

            Optional<RefreshToken> result = refreshTokenRepository.findByToken(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should propagate exception if EntityManager fails")
        void whenEntityManagerThrows_ShouldPropagateException() {
            when(entityManager.createQuery(anyString(), eq(RefreshToken.class)))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> refreshTokenRepository.findByToken("token"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB error");
        }

        @Test
        @DisplayName("Should verify correct JPQL query")
        void shouldUseCorrectJpqlQuery() {
            String tokenValue = "test-token";
            when(entityManager.createQuery(anyString(), eq(RefreshToken.class))).thenReturn(typedQuery);
            when(typedQuery.setParameter("token", tokenValue)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

            refreshTokenRepository.findByToken(tokenValue);

            verify(entityManager).createQuery(
                    FIND_BY_TOKEN_QUERY,
                    RefreshToken.class
            );
        }
    }

    @Nested
    class DeleteTokenByUserTests {

        @Test
        @DisplayName("Should delete all refresh tokens for a user")
        void whenUserProvided_ShouldExecuteDeleteQuery() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("user", testUser)).thenReturn(query);
            when(query.executeUpdate()).thenReturn(2);

            refreshTokenRepository.deleteByUser(testUser);

            verify(entityManager).createQuery("""
                    DELETE FROM RefreshToken r
                    WHERE r.user = :user
                    """);
        }

        @Test
        @DisplayName("Should handle null user gracefully")
        void whenUserIsNull_ShouldNotFail() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("user", null)).thenReturn(query);
            when(query.executeUpdate()).thenReturn(0);

            refreshTokenRepository.deleteByUser(null);
        }

        @Test
        @DisplayName("Should propagate exception from EntityManager")
        void whenEntityManagerFails_ShouldPropagateException() {
            when(entityManager.createQuery(anyString()))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> refreshTokenRepository.deleteByUser(testUser))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB error");
        }

        @Test
        @DisplayName("Should verify correct JPQL query")
        void shouldUseCorrectJpqlQuery() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("user", testUser)).thenReturn(query);
            when(query.executeUpdate()).thenReturn(1);

            refreshTokenRepository.deleteByUser(testUser);

            verify(entityManager).createQuery(
                    "DELETE FROM RefreshToken r\nWHERE r.user = :user\n"
            );
        }
    }

    @Nested
    class DeleteByTokenTests {

        @Test
        @DisplayName("Should delete refresh token by value")
        void whenTokenProvided_ShouldExecuteDeleteQuery() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("token", "token-to-delete")).thenReturn(query);
            when(query.executeUpdate()).thenReturn(1);

            refreshTokenRepository.deleteByToken("token-to-delete");
        }

        @Test
        @DisplayName("Should handle non-existent token")
        void whenTokenDoesNotExist_ShouldNotFail() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("token", "non-existent")).thenReturn(query);
            when(query.executeUpdate()).thenReturn(0);

            refreshTokenRepository.deleteByToken("non-existent");
        }

        @Test
        @DisplayName("Should handle null token gracefully")
        void whenTokenIsNull_ShouldNotFail() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("token", null)).thenReturn(query);
            when(query.executeUpdate()).thenReturn(0);

            refreshTokenRepository.deleteByToken(null);
        }

        @Test
        @DisplayName("Should verify correct JPQL query")
        void shouldUseCorrectJpqlQuery() {
            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter("token", "test-token")).thenReturn(query);
            when(query.executeUpdate()).thenReturn(1);

            refreshTokenRepository.deleteByToken("test-token");

            verify(entityManager).createQuery(
                    "DELETE FROM RefreshToken r\nWHERE r.token = :token\n"
            );
        }
    }

    @Nested
    class SaveTokenTests {

        @Test
        @DisplayName("Should persist and return refresh token")
        void whenTokenProvided_ShouldPersist() {
            doNothing().when(entityManager).persist(testRefreshToken);

            RefreshToken result = refreshTokenRepository.save(testRefreshToken);

            assertThat(result).isEqualTo(testRefreshToken);
        }

        @Test
        @DisplayName("Should persist new refresh token without ID")
        void whenNewToken_ShouldPersist() {
            RefreshToken newToken = RefreshToken.builder()
                    .token("new-token-123")
                    .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                    .user(testUser)
                    .build();

            doNothing().when(entityManager).persist(newToken);

            RefreshToken result = refreshTokenRepository.save(newToken);

            assertThat(result).isEqualTo(newToken);
        }

        @Test
        @DisplayName("Should handle null token in save")
        void whenNullToken_ShouldReturnNull() {
            doNothing().when(entityManager).persist(null);

            RefreshToken result = refreshTokenRepository.save(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should propagate exception during persist")
        void whenEntityManagerFails_ShouldPropagateException() {
            doThrow(new RuntimeException("Persistence failed")).when(entityManager).persist(testRefreshToken);

            assertThatThrownBy(() -> refreshTokenRepository.save(testRefreshToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Persistence failed");
        }
    }
}
