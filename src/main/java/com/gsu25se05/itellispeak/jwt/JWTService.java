package com.gsu25se05.itellispeak.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.auth.InvalidToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTService {
    private static final String SECRET = "CRAZY@IWASCRAZYONCE@THEYPUTMEINARUBBERROOM@ARUBBERROOMWITHRATS@ANDRATSMAKEMECRAZY!@$";
    private final long EXPIRATION_TIME = 864_000_000; // 10 days in milliseconds
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 604_800_000; // 7 days in milliseconds

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getEncoder().encode(SECRET.getBytes());
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public Cookie createTokenCookie(String token) {
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true); // Prevents client-side JavaScript access
        cookie.setSecure(true);   // Use only over HTTPS in production
        cookie.setPath("/");      // Accessible across the application
        cookie.setMaxAge((int) EXPIRATION_TIME / 1000); // Cookie expiration time
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_TIME / 1000); // Match refresh token expiration
        return cookie;
    }

    public String generateToken(String email) {
        Date now = new Date(); // get current time
        long EXPIRATION = 2 * 24 * 60 * 60 * 1000;
        Date expirationDate = new Date(now.getTime() + EXPIRATION);

        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                //id if necessary
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, getSigningKey())
                .compact();
        return token;
    }

    public String generateRefreshToken(String email) {
        Date now = new Date(); // get current time
        long EXPIRATION_REFRESHTOKEN = 7 * 24 * 60 * 60 * 1000;
        Date expirationDate = new Date(now.getTime() + EXPIRATION_REFRESHTOKEN);

        String refresh_token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, getSigningKey())
                .compact();
        return refresh_token;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            Instant expiredOn = e.getClaims().getExpiration().toInstant();
            throw new TokenExpiredException("Token has expired", expiredOn);
        } catch (JwtException e) {
            throw new InvalidToken("Invalid token");
        } catch (Exception e) {
            throw new InvalidToken("Error parsing token: " + e.getMessage());
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before((new Date()));
    }

    public Boolean validateToken(String token, User userDetails){
        final String userName= extractEmail(token);
        return (userName.equals(userDetails.getEmail()) && !isTokenExpired(token));
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> getPayload(String token) throws IOException {
        DecodedJWT decodedJWT = JWT.decode(token);
        String payload = new String(Base64.getUrlDecoder().decode(decodedJWT.getPayload()));
        return objectMapper.readValue(payload, Map.class);
    }
}
