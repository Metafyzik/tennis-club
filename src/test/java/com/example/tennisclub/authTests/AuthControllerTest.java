package com.example.tennisclub.authTests;

import com.example.tennisclub.auth.AuthController;
import com.example.tennisclub.auth.AuthService;
import com.example.tennisclub.auth.dto.LogRequestDto;
import com.example.tennisclub.auth.dto.RegistRequestDto;
import com.example.tennisclub.auth.dto.TokenResponseDto;
import com.example.tennisclub.auth.refreshToken.dto.RefreshTokenRequestDto;
import com.example.tennisclub.auth.security.JwtUtil;
import com.example.tennisclub.auth.security.SecurityConfig;
import com.example.tennisclub.user.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    class RegisterTests {

        @Test
        void validRequest_ReturnsCreated() throws Exception {
            RegistRequestDto request = new RegistRequestDto("testuser", "1234567890", "password123");
            doNothing().when(authService).register(any(RegistRequestDto.class));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("User registered successfully."));

            verify(authService).register(request);
        }

        @Test
        void invalidRequest_ReturnsBadRequest() throws Exception {
            RegistRequestDto request = new RegistRequestDto("", "1234567890", "password123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        void userWithUsernameAlreadyExists_ThrowsException() throws Exception {
            RegistRequestDto request = new RegistRequestDto("existinguser", "1234567890", "password123");

            doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username " + request.username() + " already taken"))
                    .when(authService).register(any(RegistRequestDto.class));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Username " + request.username() + " already taken"));

            verify(authService).register(request);
        }

        @Test
        void userWithPhoneNumberAlreadyExists_ThrowsException() throws Exception {
            RegistRequestDto request = new RegistRequestDto("newuser", "+420123456789", "password123");

            doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Phone number " + request.phoneNumber() + " already taken"))
                    .when(authService).register(any(RegistRequestDto.class));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Phone number " + request.phoneNumber() + " already taken"));

            verify(authService).register(request);
        }
    }

    @Nested
    class LoginTests {

        @Test
        void validCredentials_ReturnsAuthResponse() throws Exception {
            LogRequestDto request = new LogRequestDto("testuser", "password123");
            TokenResponseDto response = new TokenResponseDto("access-accessToken", "refresh-accessToken");

            when(authService.login(any(LogRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-accessToken"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-accessToken"));

            verify(authService).login(request);
        }

        @Test
        void invalidCredentials_ReturnsUnauthorized() throws Exception {
            LogRequestDto request = new LogRequestDto("testuser", "wrongpassword");

            when(authService.login(any(LogRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(authService).login(request);
        }

        @Test
        void emptyUsername_ReturnsBadRequest() throws Exception {
            LogRequestDto request = new LogRequestDto("", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.username").value("Username must not be blank"));

            verify(authService, never()).login(any());
        }

        @Test
        void emptyPassword_ReturnsBadRequest() throws Exception {
            LogRequestDto request = new LogRequestDto("testuser", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.password").value("Password must not be blank"));

            verify(authService, never()).login(any());
        }
    }

    @Nested
    class RefreshTokenTests {

        @Test
        void validToken_ReturnsNewTokens() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("valid-refresh-accessToken");
            TokenResponseDto response = new TokenResponseDto("new-access-accessToken", "new-refresh-accessToken");

            when(authService.refresh(any(RefreshTokenRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-accessToken"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh-accessToken"));

            verify(authService).refresh(request);
        }

        @Test
        void invalidToken_ReturnsForbidden() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid-refresh-accessToken");

            when(authService.refresh(any(RefreshTokenRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh accessToken"));

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Invalid refresh accessToken"));

            verify(authService).refresh(request);
        }

        @Test
        void expiredToken_ReturnsForbidden() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("expired-refresh-accessToken");

            when(authService.refresh(any(RefreshTokenRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid accessToken"));

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Invalid accessToken"));

            verify(authService).refresh(request);
        }

        @Test
        void emptyToken_ReturnsBadRequest() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.refreshToken").value("refreshToken name must not be blank"));

            verify(authService, never()).refresh(any());
        }
    }

    @Nested
    class LogoutTests {

        @Test
        void validToken_ReturnsSuccess() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("valid-refresh-accessToken");
            doNothing().when(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logged out successfully"));

            verify(authService).logout("valid-refresh-accessToken");
        }

        @Test
        void invalidToken_ReturnsBadRequest() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid-refresh-accessToken");

            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid refresh accessToken"))
                    .when(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService).logout("invalid-refresh-accessToken");
        }

        @Test
        void emptyToken_ReturnsBadRequest() throws Exception {
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("");

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.refreshToken").value("refreshToken name must not be blank"));

            verify(authService, never()).logout(anyString());
        }
    }
}