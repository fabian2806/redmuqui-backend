package com.redmuqui.platform.auth.service;

import com.redmuqui.platform.auth.dto.LoginRequest;
import com.redmuqui.platform.auth.dto.RefreshTokenRequest;
import com.redmuqui.platform.auth.dto.TokenResponse;
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

import java.time.LocalDateTime;

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

    public void logout(String accessToken) {
        // TODO: Implementar invalidación de tokens.
        // Opciones: lista negra en Redis, tabla de tokens revocados en BD.
        // Por ahora es no-op; el cliente debe descartar el token.
        log.info("Logout solicitado (no-op por ahora)");
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
