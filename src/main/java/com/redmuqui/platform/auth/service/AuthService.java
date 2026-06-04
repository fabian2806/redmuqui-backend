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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRevocationService tokenRevocationService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        var auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.contrasenha())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        usuarioRepository.findByEmailIgnoreCase(request.email()).ifPresent(u -> {
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
        });

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        try {
            String refreshToken = request.refreshToken();
            String email = jwtService.extractUsername(refreshToken);

            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new BusinessException("El token proporcionado no es un refresh token");
            }

            if (tokenRevocationService.isTokenRevoked(refreshToken)) {
                log.warn("Intento de reutilizar un refresh token revocado para {}", email);
                throw new BusinessException("Refresh token revocado");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new BusinessException("Refresh token invalido o expirado");
            }

            tokenRevocationService.revokeToken(refreshToken);

            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            return TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpirationMs());
        } catch (JwtException ex) {
            throw new BusinessException("Refresh token malformado o invalido", ex);
        }
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        tokenRevocationService.revokeToken(refreshToken);
        tokenRevocationService.revokeToken(accessToken);
    }

    public void requestRecovery(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario != null) {
            String resetToken = jwtService.generatePasswordResetToken(email);
            log.info("Token de reseteo generado para {}: {}", email, resetToken);
            log.info("Enlace de reseteo (placeholder): https://app.redmuqui.com/reset-password?token={}", resetToken);
        }
    }

    @Transactional
    public void resetPassword(String token, String nuevaContrasenha) {
        try {
            String email = jwtService.validatePasswordResetToken(token);

            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            String hashedPassword = passwordEncoder.encode(nuevaContrasenha);

            usuario.setContrasenhaHash(hashedPassword);
            usuarioRepository.save(usuario);

            log.info("Contrasenha reseteada exitosamente para usuario: {}", email);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Token de reseteo invalido o expirado", ex);
        } catch (Exception ex) {
            log.error("Error al resetear contrasenha", ex);
            throw new BusinessException("Error interno al procesar el reseteo de contrasenha", ex);
        }
    }
}
