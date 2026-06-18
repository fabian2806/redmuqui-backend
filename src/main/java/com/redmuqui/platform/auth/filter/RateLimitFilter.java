package com.redmuqui.platform.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OWASP A07 – Identification and Authentication Failures:
 *   Rate limiting por IP para los endpoints de autenticación.
 *
 *   Limita a MAX_REQUESTS peticiones por IP en una ventana de WINDOW_SECONDS.
 *   Si se supera el límite, responde 429 Too Many Requests.
 *
 *   Para producción con alta carga o múltiples instancias, reemplazar este
 *   almacenamiento en memoria por Redis + Bucket4j (añadir al pom.xml):
 *
 *     <dependency>
 *       <groupId>com.bucket4j</groupId>
 *       <artifactId>bucket4j-redis</artifactId>
 *       <version>8.10.1</version>
 *     </dependency>
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // 10 intentos por IP en una ventana de 60 segundos.
    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_SECONDS = 60;

    private record IpWindow(AtomicInteger count, Instant windowStart) {}

    private final ConcurrentHashMap<String, IpWindow> ipWindowMap = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo aplica a endpoints de autenticación.
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = obtenerIpCliente(request);
        Instant ahora = Instant.now();

        IpWindow ventana = ipWindowMap.compute(ip, (key, existing) -> {
            if (existing == null ||
                    ahora.getEpochSecond() - existing.windowStart().getEpochSecond() >= WINDOW_SECONDS) {
                // Nueva ventana.
                return new IpWindow(new AtomicInteger(1), ahora);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        int intentos = ventana.count().get();

        if (intentos > MAX_REQUESTS) {
            log.warn("[SECURITY] Rate limit superado para IP={} en path={}", ip, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            response.getWriter().write(
                    "{\"message\":\"Demasiados intentos. Espere " + WINDOW_SECONDS + " segundos.\",\"status\":429}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae la IP real del cliente respetando proxies inversos (X-Forwarded-For).
     * Solo se confía en el primer IP de la cadena para evitar spoofing.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}