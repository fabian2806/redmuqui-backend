package com.redmuqui.platform.auth.service;

import com.redmuqui.platform.auth.dto.LoginRequest;
import com.redmuqui.platform.auth.dto.RefreshTokenRequest;
import com.redmuqui.platform.auth.dto.TokenResponse;
import com.redmuqui.platform.auth.entity.RefreshTokenRevocado;
import com.redmuqui.platform.auth.repository.RefreshTokenRevocadoRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Lógica de autenticación: login, refresh, logout, recuperación.
 *
 * NOTA: logout y recuperación están como esqueleto.
 *   - Logout: requiere implementar lista negra de tokens (con Redis o tabla en BD)
 *     o aceptar el trade-off de que los tokens son válidos hasta su expiración natural.
 *   - Recover: requiere integración con servicio de correo (a definir con el equipo).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRevocadoRepository refreshTokenRevocadoRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // Spring Security valida credenciales contra CustomUserDetailsService
        var auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.contrasenha())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        // Actualizar último acceso
        usuarioRepository.findByEmailIgnoreCase(request.email()).ifPresent(u -> {
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
        });

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        try {
            String email = jwtService.extractUsername(request.refreshToken());

            if (!jwtService.isRefreshToken(request.refreshToken())) {
                throw new BusinessException("El token proporcionado no es un refresh token");
            }

            if (refreshTokenRevocadoRepository.existsByTokenHash(sha256(request.refreshToken()))) {
                throw new BusinessException("Refresh token revocado");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(request.refreshToken(), userDetails)) {
                throw new BusinessException("Refresh token inválido o expirado");
            }

            String newAccessToken = jwtService.generateAccessToken(userDetails);
            // Política: emitir nuevo refresh token también (refresh token rotation)
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            return TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpirationMs());

        } catch (JwtException ex) {
            throw new BusinessException("Refresh token malformado o inválido", ex);
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("Logout solicitado sin refresh token; no-op.");
            return;
        }
        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                log.warn("Logout recibió un token que no es refresh; ignorando.");
                return;
            }
            String hash = sha256(refreshToken);
            if (refreshTokenRevocadoRepository.existsByTokenHash(hash)) {
                return; // ya revocado, idempotente
            }
            LocalDateTime expiresAt = jwtService.extractExpiration(refreshToken)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            refreshTokenRevocadoRepository.save(
                RefreshTokenRevocado.builder()
                    .tokenHash(hash)
                    .revokedAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .build()
            );
        } catch (JwtException ex) {
            log.warn("Logout con refresh token inválido/expirado; no se registra revocación.");
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

    public void requestRecovery(String email) {
        // Generar token JWT de reseteo de contraseña
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario != null) {
            String resetToken = jwtService.generatePasswordResetToken(email);
            // TODO: Enviar por correo el enlace de reseteo
            // Por ahora, loguear el enlace para desarrollo/testing
            log.info("Token de reseteo generado para {}: {}", email, resetToken);
            log.info("Enlace de reseteo (placeholder): https://app.redmuqui.com/reset-password?token={}", resetToken);
        }
        // No revelar si el correo existe o no (best practice de seguridad).
    }

    @Transactional
    public void resetPassword(String token, String nuevaContrasenha) {
        try {
            // Validar token JWT de reseteo
            String email = jwtService.validatePasswordResetToken(token);

            // Buscar usuario
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            // Hashear nueva contraseña
            String hashedPassword = passwordEncoder.encode(nuevaContrasenha);

            // Actualizar contraseña
            usuario.setContrasenhaHash(hashedPassword);
            usuarioRepository.save(usuario);

            log.info("Contraseña reseteada exitosamente para usuario: {}", email);

        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Token de reseteo inválido o expirado", ex);
        } catch (Exception ex) {
            log.error("Error al resetear contraseña", ex);
            throw new BusinessException("Error interno al procesar el reseteo de contraseña", ex);
        }
    }
}
