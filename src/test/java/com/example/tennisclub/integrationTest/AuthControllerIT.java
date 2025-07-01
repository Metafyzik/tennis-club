package com.example.tennisclub.integrationTest;

import com.example.tennisclub.auth.dto.LogRequestDto;
import com.example.tennisclub.auth.dto.RegistRequestDto;
import com.example.tennisclub.auth.dto.TokenResponseDto;
import com.example.tennisclub.auth.refreshToken.RefreshTokenService;
import com.example.tennisclub.auth.refreshToken.dto.RefreshTokenRequestDto;
import com.example.tennisclub.auth.security.JwtUtil;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.UserService;
import com.example.tennisclub.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void register_ShouldCreateUserSuccessfully() {
        RegistRequestDto request = new RegistRequestDto("newuser", "1234567890", "password");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully.", response.getBody());

        assertTrue(userService.userWithUsernameExistsRegardlessOfDeletion("newuser"));
    }

    @Test
    void register_ShouldThrowErrorWhenUsernameExist(){
        var username = "newuser";

        RegistRequestDto request = new RegistRequestDto(username, "1234567890", "password");
        restTemplate.postForEntity("/api/auth/register", request, String.class);

        RegistRequestDto requestWithTheSameUsername = new RegistRequestDto(username, "11111112", "password");
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", requestWithTheSameUsername, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Username "+username+" already taken"));
    }

    @Test
    void register_then_login_successfully(){
        //new user
        String username = "newUser";
        String phoneNumber = "1434567890";
        String password = "password";

        RegistRequestDto request = new RegistRequestDto(username, phoneNumber, password);

        restTemplate.postForEntity("/api/auth/register", request, String.class);

        LogRequestDto login = new LogRequestDto(username, password);

        ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity("/api/auth/login", login, TokenResponseDto.class);

        System.out.println("the response is");
        System.out.println(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().accessToken());
        assertNotNull(response.getBody().refreshToken());
    }

    @Test
    void refresh_ShouldReturnNewTokens() {
        User user = User.builder()
                .username("refreshuser")
                .phoneNumber("1112223333")
                .password(passwordEncoder.encode("refreshpass"))
                .roles(Set.of(Role.MEMBER))
                .build();
        userService.save(user);

        String refreshToken = jwtUtil.generateRefreshToken();
        refreshTokenService.createRefreshToken(user, refreshToken);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(refreshToken);

        ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity("/api/auth/refresh", request, TokenResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().accessToken());
        assertNotNull(response.getBody().refreshToken());
    }

    @Test
    void login_ShouldReturnAccessAndRefreshTokens() {

        User user = User.builder()
                .username("member")
                .phoneNumber("7534567890")
                .password(passwordEncoder.encode("pass"))
                .roles(Set.of(Role.MEMBER))
                .build();
        userService.save(user);

        LogRequestDto login = new LogRequestDto("member", "pass");

        ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity("/api/auth/login", login, TokenResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().accessToken());
        assertNotNull(response.getBody().refreshToken());
    }

    @Test
    void logout_ShouldInvalidateRefreshToken() {
        User user = User.builder()
                .username("logoutuser")
                .phoneNumber("9998887777")
                .password(passwordEncoder.encode("logoutpass"))
                .roles(Set.of(Role.MEMBER))
                .build();
        userService.save(user);

        String refreshToken = jwtUtil.generateRefreshToken();
        refreshTokenService.createRefreshToken(user, refreshToken);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(refreshToken);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/logout", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out successfully", response.getBody());

        assertTrue(refreshTokenService.findByToken(refreshToken).isEmpty());
    }
}
