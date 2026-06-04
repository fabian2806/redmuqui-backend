package com.redmuqui.platform.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para emisión y validación de JSON Web Tokens.
 *
 * Maneja dos tipos de token:
 *   - access token: corta vida, usado en cada request en header Authorization.
 *   - refresh token: vida más larga, usado para emitir nuevos access tokens (RF-003).
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.reset-token-expiration-ms}")
    private long resetTokenExpirationMs;

    @Value("${app.jwt.issuer}")
    private String issuer;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return buildToken(claims, userDetails.getUsername(), accessTokenExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildToken(claims, userDetails.getUsername(), refreshTokenExpirationMs);
    }

    public String generatePasswordResetToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "password_reset");
        return buildToken(claims, email, resetTokenExpirationMs);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    public boolean isPasswordResetToken(String token) {
        return "password_reset".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    public String validatePasswordResetToken(String token) {
        if (!isPasswordResetToken(token)) {
            throw new IllegalArgumentException("Token no es de tipo password reset");
        }
        if (isTokenExpired(token)) {
            throw new IllegalArgumentException("Token de reseteo expirado");
        }
        return extractUsername(token);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuer(issuer)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
