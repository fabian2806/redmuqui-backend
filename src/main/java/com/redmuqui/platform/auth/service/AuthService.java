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
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private final EmailService emailService;
    private static final int MAX_INTENTOS_LOGIN = 5;
    private static final int MINUTOS_BLOQUEO = 10;

    @Transactional(noRollbackFor = AuthenticationException.class)
    public TokenResponse login(LoginRequest request) {
        var usuarioOpt = usuarioRepository.findByEmailIgnoreCase(request.email());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (usuario.getBloqueadoHasta() != null &&
                    usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {

                Duration tiempoRestante = Duration.between(
                        LocalDateTime.now(),
                        usuario.getBloqueadoHasta()
                );
                long minutosRestantes = Math.max(1, tiempoRestante.toMinutes());

                throw new ResponseStatusException(
                        HttpStatus.LOCKED,
                        "Cuenta bloqueada temporalmente. Intente nuevamente en "
                                + minutosRestantes + " minuto(s)."
                );
            }

            if (usuario.getBloqueadoHasta() != null &&
                    usuario.getBloqueadoHasta().isBefore(LocalDateTime.now())) {
                usuario.setIntentosLoginFallidos(0);
                usuario.setBloqueadoHasta(null);
                usuarioRepository.save(usuario);
            }
        }

        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.contrasenha())
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            usuarioRepository.findByEmailIgnoreCase(request.email()).ifPresent(u -> {
                u.setUltimoAcceso(LocalDateTime.now());
                u.setIntentosLoginFallidos(0);
                u.setBloqueadoHasta(null);
                usuarioRepository.save(u);
            });

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());

        } catch (AuthenticationException ex) {
            usuarioOpt.ifPresent(this::registrarIntentoLoginFallido);
            throw ex;
        }
    }

    private void registrarIntentoLoginFallido(Usuario usuario) {
        int intentosActuales = usuario.getIntentosLoginFallidos() == null
                ? 0
                : usuario.getIntentosLoginFallidos();

        int nuevosIntentos = intentosActuales + 1;
        usuario.setIntentosLoginFallidos(nuevosIntentos);

        if (nuevosIntentos >= MAX_INTENTOS_LOGIN) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            // OWASP A09: loguear evento de seguridad SIN datos sensibles.
            log.warn("[SECURITY] Cuenta bloqueada por {} intentos fallidos para usuario id={}",
                    nuevosIntentos, usuario.getId());
        }

        usuarioRepository.save(usuario);
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
                // OWASP A09: loguear intento de reuso sin exponer el token.
                log.warn("[SECURITY] Intento de reutilizar refresh token revocado para email={}", email);
                throw new BusinessException("Refresh token revocado");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new BusinessException("Refresh token invalido o expirado");
            }

            if (tokenEmitidoAntesDelCambioContrasenha(refreshToken, usuario)) {
                tokenRevocationService.revokeToken(refreshToken);
                throw new BusinessException("Refresh token revocado por cambio de contrasena");
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

    /**
     * OWASP A09 – Security Logging:
     *   ANTES: el token de reseteo se logueaba en texto plano, permitiendo que
     *   cualquier persona con acceso a los logs pudiera resetear contraseñas.
     *
     *   AHORA: se registra el evento sin exponer el token, y el token se envia
     *   por correo mediante EmailService.
     *
     * OWASP A04 – Insecure Design:
     *   El enlace de reseteo debe caducar (ya implementado vía JWT) y el token
     *   debe invalidarse tras su primer uso (pendiente de implementar).
     */
    public void requestRecovery(String email) {
        // Respuesta homogénea: no revelar si el email existe o no (OWASP A01).
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        if (usuario != null) {
            String resetToken = jwtService.generatePasswordResetToken(email);
            try {
                emailService.enviarRecuperacionContrasenha(usuario.getEmail(), resetToken);
                log.info("[AUDIT] Solicitud de recuperacion de contrasena para usuario id={}", usuario.getId());
                log.info("Correo de recuperacion enviado a {}", usuario.getEmail());
            } catch (Exception ex) {
                log.error("No se pudo enviar el correo de recuperacion a {}", usuario.getEmail(), ex);
            }
        }
        // Si el usuario no existe, no se hace nada ni se logea: respuesta idéntica.
    }

    @Transactional
    public void resetPassword(String token, String nuevaContrasenha) {
        try {
            String email = jwtService.validatePasswordResetToken(token);

            if (tokenRevocationService.isTokenRevoked(token)) {
                throw new BusinessException("Token de reseteo invalido o expirado");
            }

            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (!tokenRevocationService.revokeTokenIfNotRevoked(token)) {
                throw new BusinessException("Token de reseteo invalido o expirado");
            }

            usuario.setContrasenhaHash(passwordEncoder.encode(nuevaContrasenha));
            usuario.setContrasenhaActualizadaEn(LocalDateTime.now());
            usuarioRepository.save(usuario);

            log.info("[AUDIT] Contraseña reseteada para usuario id={}", usuario.getId());

        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Token de reseteo invalido o expirado", ex);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            // OWASP A09: loguear el error real internamente, nunca exponer al cliente.
            log.error("[ERROR] Error al resetear contraseña", ex);
            throw new BusinessException("Error interno al procesar el reseteo de contraseña");
        }
    }

    private boolean tokenEmitidoAntesDelCambioContrasenha(String token, Usuario usuario) {
        LocalDateTime contrasenhaActualizadaEn = usuario.getContrasenhaActualizadaEn();
        if (contrasenhaActualizadaEn == null) {
            return false;
        }

        LocalDateTime emitidoEn = jwtService.extractIssuedAt(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return emitidoEn.isBefore(contrasenhaActualizadaEn);
    }
}
