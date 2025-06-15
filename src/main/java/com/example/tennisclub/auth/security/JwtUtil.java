package com.example.tennisclub.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getKey()));
    }

    // Generate short-lived access accessToken
    public String generateAccessToken(UserDetails userDetails) {
        Instant expiry = Instant.now().plus(jwtProperties.getAccessTokenDuration());

        return Jwts.builder()
                .claims()
                .subject(userDetails.getUsername())
                .expiration(Date.from(expiry))
                .and()
                .signWith(getSignKey())
                .compact();
    }

    // Generate long-lived refresh accessToken (simple UUID-based)
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver) {
        return claimsResolver.apply(Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload());
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, claims -> claims.getExpiration().before(new Date()));
    }
}