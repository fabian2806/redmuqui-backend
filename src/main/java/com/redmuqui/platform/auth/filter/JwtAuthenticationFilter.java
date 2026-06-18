package com.redmuqui.platform.auth.filter;

import com.redmuqui.platform.auth.service.JwtService;
import com.redmuqui.platform.auth.service.TokenRevocationService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que extrae el JWT del header Authorization y autentica al usuario.
 *
 * OWASP A09 – Security Logging:
 *   Eliminado ex.printStackTrace() que volcaba stack traces en stdout (riesgo
 *   de exponer rutas de clase, versiones de librerías y lógica interna).
 *   Ahora se usa log.warn() con mensaje descriptivo sin información sensible.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null &&
                    (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken)) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isAccessToken(jwt)
                        && !tokenRevocationService.isTokenRevoked(jwt)
                        && jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException ex) {
            // OWASP A09: warn sin stack trace (el mensaje de jjwt ya es suficiente).
            log.warn("[SECURITY] Token JWT inválido o expirado: {}", ex.getMessage());
        } catch (Exception ex) {
            // OWASP A09: loguear con nivel warn; nunca imprimir stack trace en stdout.
            log.warn("[SECURITY] Error procesando token JWT: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}