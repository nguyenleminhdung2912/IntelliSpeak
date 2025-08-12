package com.gsu25se05.itellispeak.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String extractUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token format
        if (token.split("\\.").length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }

        try {
            // Ensure the secret key is long enough for HS512
            if (jwtSecret.length() < 64) {
                throw new IllegalStateException("JWT secret key must be at least 64 characters for HS512");
            }

            // Base64-encode the secret to match JWTService's signing process
            byte[] keyBytes = Base64.getEncoder().encode(jwtSecret.getBytes(StandardCharsets.UTF_8));
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Use "sub" to match the token's payload
            String userId = claims.get("sub", String.class);
            if (userId == null) {
                throw new JwtException("Subject (sub) not found in token");
            }
            return userId;
        } catch (JwtException e) {
            throw new JwtException("Invalid or expired JWT token: " + e.getMessage());
        }
    }
}