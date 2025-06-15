package com.example.tennisclub.auth;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @Transactional
    public void register(RegistRequestDto request) {
        if (userService.userWithUsernameExist(request.username())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Username " +request.username()+ " already taken");
        }

        if (userService.userWithPhoneNumberExist(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Phone number " +request.phoneNumber()+ " already taken");
        }

        User user = User.builder()
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.MEMBER))
                .build();

        userService.save(user);
    }

    public TokenResponseDto login(LogRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            User user = userService.findByUsernameOrThrow(request.username());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();


            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshTokenValue = jwtUtil.generateRefreshToken();
            refreshTokenService.createRefreshToken(user, refreshTokenValue);

            return new TokenResponseDto(accessToken, refreshTokenValue);
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    public TokenResponseDto refresh(RefreshTokenRequestDto request) {

        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                    String newAccessToken = jwtUtil.generateAccessToken(userDetails);
                    String newRefreshToken = jwtUtil.generateRefreshToken();
                    refreshTokenService.deleteByToken(request.refreshToken());
                    refreshTokenService.createRefreshToken(user, newRefreshToken);

                    return new TokenResponseDto(newAccessToken, newRefreshToken);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh accessToken"));
    }

    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid refresh accessToken"));
        refreshTokenService.deleteByToken(refreshToken);
    }
}