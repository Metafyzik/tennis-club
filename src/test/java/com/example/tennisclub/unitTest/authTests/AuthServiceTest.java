package com.example.tennisclub.unitTest.authTests;

import com.example.tennisclub.auth.AuthService;
import com.example.tennisclub.auth.dto.LogRequestDto;
import com.example.tennisclub.auth.dto.RegistRequestDto;
import com.example.tennisclub.auth.dto.TokenResponseDto;
import com.example.tennisclub.auth.refreshToken.RefreshTokenService;
import com.example.tennisclub.auth.refreshToken.dto.RefreshTokenRequestDto;
import com.example.tennisclub.auth.refreshToken.entity.RefreshToken;
import com.example.tennisclub.auth.security.JwtUtil;
import com.example.tennisclub.user.CustomUserDetailsService;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.UserService;
import com.example.tennisclub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private UserService userService;

    @InjectMocks private AuthService authService;

    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .phoneNumber("1234567890")
                .password("encoded-password")
                .roles(Set.of(Role.MEMBER))
                .build();

        testUserDetails = new org.springframework.security.core.userdetails.User(
                "testuser", "encoded-password",
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    @Nested
    class RegisterTests {

        @Test
        void validRequest_CreatesUser() {
            RegistRequestDto request = new RegistRequestDto("newuser", "1234567890", "password123");
            when(userService.userWithUsernameExistsRegardlessOfDeletion("newuser")).thenReturn(false);
            when(userService.userWithPhoneNumberExistsRegardlessOfDeletion("1234567890")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(userService.save(any(User.class))).thenReturn(testUser);

            assertDoesNotThrow(() -> authService.register(request));

            verify(userService).userWithUsernameExistsRegardlessOfDeletion("newuser");
            verify(userService).userWithPhoneNumberExistsRegardlessOfDeletion("1234567890");
            verify(passwordEncoder).encode("password123");
            verify(userService).save(any(User.class));
        }

        @Test
        void usernameAlreadyExists_ThrowsConflict() {
            RegistRequestDto request = new RegistRequestDto("existinguser", "1234567890", "password123");
            when(userService.userWithUsernameExistsRegardlessOfDeletion("existinguser")).thenReturn(true);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.register(request));

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Username existinguser already taken", exception.getReason());
        }

        @Test
        void phoneNumberAlreadyExists_ThrowsConflict() {
            RegistRequestDto request = new RegistRequestDto("newuser", "1234567890", "password123");
            when(userService.userWithUsernameExistsRegardlessOfDeletion("newuser")).thenReturn(false);
            when(userService.userWithPhoneNumberExistsRegardlessOfDeletion("1234567890")).thenReturn(true);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.register(request));

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Phone number 1234567890 already taken", exception.getReason());
        }

        @Test
        void softDeletedUserWithSameUsername_ThrowsConflict() {
            RegistRequestDto request = new RegistRequestDto("softdeleteduser", "9876543210", "pass");

            // Simulate a soft-deleted user still existing
            when(userService.userWithUsernameExistsRegardlessOfDeletion("softdeleteduser")).thenReturn(true);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.register(request));

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Username softdeleteduser already taken", exception.getReason());
        }

    }

    @Nested
    class LoginTests {

        @Test
        void validCredentials_ReturnsTokens() {
            LogRequestDto request = new LogRequestDto("testuser", "password123");
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(testUserDetails);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userService.findByUsernameOrThrow("testuser")).thenReturn(testUser);
            when(jwtUtil.generateAccessToken(testUserDetails)).thenReturn("access-token");
            when(jwtUtil.generateRefreshToken()).thenReturn("refresh-token");

            TokenResponseDto result = authService.login(request);

            assertNotNull(result);
            assertEquals("access-token", result.accessToken());
            assertEquals("refresh-token", result.refreshToken());

            verify(refreshTokenService).createRefreshToken(testUser, "refresh-token");
        }

        @Test
        void invalidCredentials_ThrowsUnauthorized() {
            LogRequestDto request = new LogRequestDto("testuser", "wrongpassword");
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.login(request));

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("Invalid credentials", exception.getReason());
        }
    }

    @Nested
    class RefreshTokenTests {

        @Test
        void validToken_ReturnsNewTokens() {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("valid-refresh-token");
            RefreshToken token = RefreshToken.builder()
                    .token("valid-refresh-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(token));
            when(refreshTokenService.verifyExpiration(token)).thenReturn(token);
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUserDetails);
            when(jwtUtil.generateAccessToken(testUserDetails)).thenReturn("new-access-token");
            when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");

            TokenResponseDto result = authService.refresh(request);

            assertEquals("new-access-token", result.accessToken());
            assertEquals("new-refresh-token", result.refreshToken());
        }

        @Test
        void invalidToken_ThrowsForbidden() {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid-token");
            when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.refresh(request));

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
            assertEquals("Invalid refresh accessToken", exception.getReason());
        }

        @Test
        void expiredToken_ThrowsForbidden() {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("expired-token");
            RefreshToken token = RefreshToken.builder()
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(Instant.now().minusSeconds(3600))
                    .build();

            when(refreshTokenService.findByToken("expired-token")).thenReturn(Optional.of(token));
            when(refreshTokenService.verifyExpiration(token))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token expired. Please sign in again."));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.refresh(request));

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        }
    }

    @Nested
    class LogoutTests {

        @Test
        void validToken_DeletesToken() {
            String tokenValue = "valid-refresh-token";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenService.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertDoesNotThrow(() -> authService.logout(tokenValue));
            verify(refreshTokenService).deleteByToken(tokenValue);
        }

        @Test
        void invalidToken_ThrowsBadRequest() {
            String tokenValue = "invalid-token";
            when(refreshTokenService.findByToken(tokenValue)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> authService.logout(tokenValue));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid refresh accessToken", exception.getReason());
        }
    }
}