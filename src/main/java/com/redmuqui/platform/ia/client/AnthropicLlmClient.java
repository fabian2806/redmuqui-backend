package com.redmuqui.platform.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link LlmClient} sobre la API de Anthropic (Claude),
 * endpoint {@code /v1/messages} de la Messages API.
 *
 * <p>Alternativa a {@link GeminiLlmClient}: se activa cuando
 * {@code app.ia.provider=anthropic}. La lógica de negocio ({@code ResumenIaService})
 * no cambia porque depende de la interfaz {@link LlmClient}, no del proveedor.</p>
 *
 * <p>La API key se inyecta por entorno ({@code ANTHROPIC_API_KEY}) y NUNCA se
 * versiona. Sin key configurada, {@link #estaConfigurado()} devuelve {@code false}
 * y el servicio cae a un resumen-plantilla en vez de fallar.</p>
 */
@Component
@ConditionalOnProperty(name = "app.ia.provider", havingValue = "anthropic")
public class AnthropicLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicLlmClient.class);

    private final String apiKey;
    private final String modelo;
    private final String baseUrl;
    private final String anthropicVersion;
    private final int maxOutputTokens;
    private final int maxReintentos;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicLlmClient(
            @Value("${app.ia.anthropic.api-key:}") String apiKey,
            @Value("${app.ia.anthropic.model:claude-sonnet-4-6}") String modelo,
            @Value("${app.ia.anthropic.base-url:https://api.anthropic.com/v1}") String baseUrl,
            @Value("${app.ia.anthropic.version:2023-06-01}") String anthropicVersion,
            @Value("${app.ia.anthropic.max-tokens:2048}") int maxOutputTokens,
            @Value("${app.ia.anthropic.timeout-ms:30000}") int timeoutMs,
            @Value("${app.ia.anthropic.max-reintentos:2}") int maxReintentos) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelo = modelo;
        this.baseUrl = baseUrl;
        this.anthropicVersion = anthropicVersion;
        this.maxOutputTokens = maxOutputTokens;
        this.maxReintentos = Math.max(0, maxReintentos);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    /** Deja constancia en el log de arranque de si la IA quedó configurada (sin exponer la key). */
    @PostConstruct
    void logEstado() {
        log.info("Resumen IA — proveedor Anthropic configurado: {} (modelo={}, longitud de la key={})",
            estaConfigurado(), modelo, apiKey.length());
    }

    @Override
    public boolean estaConfigurado() {
        return !apiKey.isBlank();
    }

    @Override
    public String modelo() {
        return modelo;
    }

    @Override
    public String generar(String instruccionSistema, String prompt) {
        String url = baseUrl + "/messages";

        // Messages API: la instrucción de sistema va en "system"; la ficha en un turno de usuario.
        Map<String, Object> body = Map.of(
            "model", modelo,
            "max_tokens", maxOutputTokens,
            "system", instruccionSistema,
            "messages", List.of(Map.of(
                "role", "user",
                "content", prompt
            ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", anthropicVersion);
        HttpEntity<Map<String, Object>> peticion = new HttpEntity<>(body, headers);

        // Reintenta ante errores transitorios (5xx, incluido el 529 "overloaded", o cortes de
        // red). Los 4xx (p.ej. 429 por cuota, 401 por key inválida) NO se reintentan: el
        // servicio cae a la plantilla, que es la respuesta correcta para esos casos.
        RuntimeException ultimoError = null;
        for (int intento = 0; intento <= maxReintentos; intento++) {
            try {
                ResponseEntity<String> respuesta =
                    restTemplate.postForEntity(url, peticion, String.class);
                return extraerTexto(respuesta.getBody());
            } catch (HttpServerErrorException | ResourceAccessException e) {
                ultimoError = e;
                log.warn("Anthropic no disponible (intento {}/{}): {}",
                    intento + 1, maxReintentos + 1, e.getMessage());
                if (intento < maxReintentos) {
                    dormir(1200L * (intento + 1));
                }
            }
        }
        throw ultimoError;
    }

    private static void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Reintento de IA interrumpido", e);
        }
    }

    /** Extrae y concatena los bloques de texto de {@code content[*].text} de la respuesta de Claude. */
    private String extraerTexto(String json) {
        try {
            JsonNode raiz = objectMapper.readTree(json);
            JsonNode contenido = raiz.path("content");
            StringBuilder sb = new StringBuilder();
            for (JsonNode bloque : contenido) {
                if ("text".equals(bloque.path("type").asText())) {
                    sb.append(bloque.path("text").asText(""));
                }
            }
            String texto = sb.toString().trim();
            if (texto.isEmpty()) {
                throw new IllegalStateException("Anthropic devolvió una respuesta sin texto");
            }
            return texto;
        } catch (Exception e) {
            log.warn("Respuesta de Anthropic no interpretable: {}", e.getMessage());
            throw new RuntimeException("No se pudo interpretar la respuesta de Anthropic", e);
        }
    }
}
