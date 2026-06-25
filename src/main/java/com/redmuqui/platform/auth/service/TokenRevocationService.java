package com.redmuqui.platform.auth.service;

import com.redmuqui.platform.auth.entity.RefreshTokenRevocado;
import com.redmuqui.platform.auth.repository.RefreshTokenRevocadoRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final JwtService jwtService;
    private final RefreshTokenRevocadoRepository refreshTokenRevocadoRepository;

    public boolean isTokenRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return refreshTokenRevocadoRepository.existsByTokenHash(sha256(token));
    }

    @Transactional
    public void revokeToken(String token) {
        revokeTokenIfNotRevoked(token);
    }

    @Transactional
    public boolean revokeTokenIfNotRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            String hash = sha256(token);
            if (refreshTokenRevocadoRepository.existsByTokenHash(hash)) {
                return false;
            }

            LocalDateTime expiresAt = jwtService.extractExpiration(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

            refreshTokenRevocadoRepository.saveAndFlush(
                RefreshTokenRevocado.builder()
                    .tokenHash(hash)
                    .revokedAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .build()
            );
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        } catch (JwtException ex) {
            log.warn("No se registro revocacion: token invalido o expirado.");
            return false;
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
