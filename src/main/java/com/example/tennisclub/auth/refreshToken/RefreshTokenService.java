package com.example.tennisclub.auth.refreshToken;


import com.example.tennisclub.auth.refreshToken.entity.RefreshToken;
import com.example.tennisclub.auth.security.JwtProperties;
import com.example.tennisclub.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public void createRefreshToken(User user, String tokenValue) {
        //prevent refresh accessToken accumulation in cases of re-login
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(Instant.now().plus(jwtProperties.getRefreshTokenDuration()))
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            deleteByToken(token.getToken());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh accessToken expired. Please sign in again.");
        }
        return token;
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}