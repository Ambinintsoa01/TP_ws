package com.apiEtudiant.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.apiEtudiant.exception.ApiException;
import com.apiEtudiant.exception.ErrorCode;

@Service
public class AuthService {

    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final Map<String, AuthToken> issuedTokens = new ConcurrentHashMap<>();
    private final Duration tokenTtl;
    private final String tokenHeaderName;

    public AuthService(
            @Value("${app.security.default-username:admin}") String defaultUsername,
            @Value("${app.security.default-password:admin123}") String defaultPassword,
            @Value("${app.security.token-ttl-seconds:3600}") long tokenTtlSeconds,
            @Value("${app.security.token-header:X-Auth-Token}") String tokenHeaderName) {
        this.users.put(defaultUsername, defaultPassword);
        this.tokenTtl = Duration.ofSeconds(Math.max(tokenTtlSeconds, 60));
        this.tokenHeaderName = tokenHeaderName;
    }

    public AuthToken authenticate(String username, String password) {
        String expectedPassword = users.get(username);
        if (expectedPassword == null || !Objects.equals(expectedPassword, password)) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Identifiants invalides.", HttpStatus.UNAUTHORIZED);
        }
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(tokenTtl);
        AuthToken authToken = new AuthToken(token, username, expiresAt);
        issuedTokens.put(token, authToken);
        return authToken;
    }

    public void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(ErrorCode.AUTH_REQUIRED, "Token d'authentification manquant.", HttpStatus.UNAUTHORIZED);
        }
        AuthToken authToken = issuedTokens.get(token);
        if (authToken == null) {
            throw new ApiException(ErrorCode.AUTH_REQUIRED, "Token invalide.", HttpStatus.UNAUTHORIZED);
        }
        if (authToken.expiresAt().isBefore(Instant.now())) {
            issuedTokens.remove(token);
            throw new ApiException(ErrorCode.AUTH_REQUIRED, "Token expiré.", HttpStatus.UNAUTHORIZED);
        }
    }

    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    public record AuthToken(String token, String username, Instant expiresAt) { }
}
