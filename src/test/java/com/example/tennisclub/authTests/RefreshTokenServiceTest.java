package com.example.tennisclub.authTests;


import com.example.tennisclub.auth.refreshToken.RefreshTokenRepository;
import com.example.tennisclub.auth.refreshToken.RefreshTokenService;
import com.example.tennisclub.auth.refreshToken.entity.RefreshToken;
import com.example.tennisclub.auth.security.JwtProperties;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .phoneNumber("1234567890")
                .password("encoded-password")
                .roles(Set.of(Role.MEMBER))
                .build();

        testRefreshToken = RefreshToken.builder()
                .token("test-refresh-token")
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
    }

    @Nested
    class CreateRefreshTokenTests {

        @Test
        void validRequest_createsAndSavesToken() {
            String tokenValue = "new-refresh-token";
            Duration duration = Duration.ofDays(7);
            when(jwtProperties.getRefreshTokenDuration()).thenReturn(duration);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

            assertDoesNotThrow(() -> refreshTokenService.createRefreshToken(testUser, tokenValue));

            verify(refreshTokenRepository).deleteByUser(testUser);
            verify(jwtProperties).getRefreshTokenDuration();
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    class FindByTokenTests {

        @Test
        void existingToken_returnsToken() {
            when(refreshTokenRepository.findByToken("existing-token")).thenReturn(Optional.of(testRefreshToken));

            Optional<RefreshToken> result = refreshTokenService.findByToken("existing-token");

            assertTrue(result.isPresent());
            assertEquals(testRefreshToken, result.get());
            verify(refreshTokenRepository).findByToken("existing-token");
        }

        @Test
        void nonExistingToken_returnsEmpty() {
            when(refreshTokenRepository.findByToken("non-existing-token")).thenReturn(Optional.empty());

            Optional<RefreshToken> result = refreshTokenService.findByToken("non-existing-token");

            assertFalse(result.isPresent());
            verify(refreshTokenRepository).findByToken("non-existing-token");
        }
    }

    @Nested
    class VerifyExpirationTests {

        @Test
        void validToken_returnsToken() {
            RefreshToken validToken = RefreshToken.builder()
                    .token("valid-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            RefreshToken result = refreshTokenService.verifyExpiration(validToken);

            assertEquals(validToken, result);
            verify(refreshTokenRepository, never()).deleteByToken(anyString());
        }

        @Test
        void expiredToken_throwsExceptionAndDeletes() {
            RefreshToken expired = RefreshToken.builder()
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(Instant.now().minusSeconds(3600))
                    .build();

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> refreshTokenService.verifyExpiration(expired));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals("Refresh accessToken expired. Please sign in again.", ex.getReason());
            verify(refreshTokenRepository).deleteByToken("expired-token");
        }

        @Test
        void tokenExpiresNow_throwsException() {
            RefreshToken expiringNow = RefreshToken.builder()
                    .token("now-token")
                    .user(testUser)
                    .expiryDate(Instant.now())
                    .build();

            assertThrows(ResponseStatusException.class,
                    () -> refreshTokenService.verifyExpiration(expiringNow));

            verify(refreshTokenRepository).deleteByToken("now-token");
        }

        @Test
        void deleteFails_stillThrowsExpirationException() {
            RefreshToken expired = RefreshToken.builder()
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(Instant.now().minusSeconds(3600))
                    .build();

            doThrow(new RuntimeException("Delete failed")).when(refreshTokenRepository).deleteByToken("expired-token");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> refreshTokenService.verifyExpiration(expired));

            assertEquals("Delete failed", ex.getMessage());
            verify(refreshTokenRepository).deleteByToken("expired-token");
        }

        @Test
        void nullToken_throwsNullPointer() {
            assertThrows(NullPointerException.class,
                    () -> refreshTokenService.verifyExpiration(null));

            verify(refreshTokenRepository, never()).deleteByToken(anyString());
        }
    }

    @Nested
    class DeleteByTokenTests {

        @Test
        void existingToken_deletesSuccessfully() {
            assertDoesNotThrow(() -> refreshTokenService.deleteByToken("token-to-delete"));
            verify(refreshTokenRepository).deleteByToken("token-to-delete");
        }

        @Test
        void nonExistingToken_doesNotThrow() {
            assertDoesNotThrow(() -> refreshTokenService.deleteByToken("non-existing-token"));
            verify(refreshTokenRepository).deleteByToken("non-existing-token");
        }

        @Test
        void nullToken_doesNotThrow() {
            assertDoesNotThrow(() -> refreshTokenService.deleteByToken(null));
            verify(refreshTokenRepository).deleteByToken(null);
        }

        @Test
        void deleteThrows_propagatesException() {
            String token = "test-token";
            doThrow(new RuntimeException("Delete failed")).when(refreshTokenRepository).deleteByToken(token);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> refreshTokenService.deleteByToken(token));

            assertEquals("Delete failed", ex.getMessage());
            verify(refreshTokenRepository).deleteByToken(token);
        }
    }
}
