package com.redmuqui.platform.config;

import com.redmuqui.platform.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuración de seguridad HTTP.
 *
 * OWASP A01 – Broken Access Control: no existe flag que deshabilite la autenticación.
 * OWASP A05 – Security Misconfiguration: se añaden security headers completos.
 * OWASP A05 – Swagger solo accesible para usuarios autenticados.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // OWASP A05: Swagger deshabilitado por defecto en prod mediante variable de entorno.
    @Value("${app.swagger.enabled:false}")
    private boolean swaggerEnabled;

    private static final String[] AUTH_ENDPOINTS = {
            "/api/v1/auth/**",
            "/actuator/health",
            "/health"
    };

    private static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deshabilitado: API stateless con JWT (sin cookies de sesión).
                .csrf(AbstractHttpConfigurer::disable)

                // Delegar CORS a CorsConfig.
                .cors(cors -> {})

                // Sin sesión HTTP: cada request se autentica con JWT.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── OWASP A05: Security Headers ──────────────────────────────────
                .headers(headers -> headers
                        // Evita que el navegador "adivine" el Content-Type.
                        .contentTypeOptions(ct -> {})
                        // Deniega embeberlo en iframes (clickjacking).
                        .frameOptions(fo -> fo.deny())
                        // XSS filter legacy (IE/Edge).
                        .xssProtection(xss ->
                                xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        // HSTS: fuerza HTTPS durante 1 año, incluye subdominios.
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31_536_000))
                        // Referrer Policy: no filtrar info sensible en cabecera Referer.
                        .referrerPolicy(ref ->
                                ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // CSP: solo permite recursos del mismo origen.
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data:; " +
                                                "frame-ancestors 'none';"
                                ))
                        // Evita cachear respuestas que puedan contener datos sensibles.
                        .cacheControl(cc -> {})
                )
                // ─────────────────────────────────────────────────────────────────

                .authorizeHttpRequests(auth -> {
                    // Endpoints siempre públicos.
                    auth.requestMatchers(AUTH_ENDPOINTS).permitAll();

                    // OWASP A01: Swagger requiere autenticación.
                    // Solo disponible si app.swagger.enabled=true (dev/staging).
                    if (swaggerEnabled) {
                        auth.requestMatchers(SWAGGER_ENDPOINTS).authenticated();
                    } else {
                        auth.requestMatchers(SWAGGER_ENDPOINTS).denyAll();
                    }

                    auth.anyRequest().authenticated();
                })

                .authenticationProvider(authenticationProvider())

                // 401 para "no autenticado"; 403 queda para "sin permiso" (@PreAuthorize).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"message\":\"No autenticado o sesión expirada\",\"status\":401}");
                        }))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 12 (OWASP A02: hashing fuerte para contraseñas).
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}