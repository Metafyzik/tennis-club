package com.example.tennisclub.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperties {
    //Base64-encoded secret key
    private String key;
    private Duration accessTokenDuration;
    private Duration refreshTokenDuration;
}