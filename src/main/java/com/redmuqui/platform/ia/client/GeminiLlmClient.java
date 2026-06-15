package com.redmuqui.platform.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link LlmClient} sobre la API de Google Gemini
 * (endpoint {@code generateContent} de Generative Language).
 *
 * <p>La API key se inyecta por variable de entorno ({@code GEMINI_API_KEY}) y
 * NUNCA se versiona, igual que {@code JWT_SECRET}. Si no hay key configurada,
 * {@link #estaConfigurado()} devuelve {@code false} y el servicio cae a un
 * resumen-plantilla en vez de fallar.</p>
 */
@Component
public class GeminiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiLlmClient.class);

    private final String apiKey;
    private final String modelo;
    private final String baseUrl;
    private final int maxReintentos;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiLlmClient(
            @Value("${app.ia.api-key:}") String apiKey,
            @Value("${app.ia.model:gemini-2.5-flash}") String modelo,
            @Value("${app.ia.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ia.timeout-ms:30000}") int timeoutMs,
            @Value("${app.ia.max-reintentos:2}") int maxReintentos) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelo = modelo;
        this.baseUrl = baseUrl;
        this.maxReintentos = Math.max(0, maxReintentos);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    /** Deja constancia en el log de arranque de si la IA quedó configurada (sin exponer la key). */
    @PostConstruct
    void logEstado() {
        log.info("Resumen IA — proveedor Gemini configurado: {} (modelo={}, longitud de la key={})",
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
        // La key viaja en cabecera (x-goog-api-key), no en la URL, para no filtrarla en logs.
        String url = baseUrl + "/models/" + modelo + ":generateContent";

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.4);
        generationConfig.put("maxOutputTokens", 2048);
        if (modelo.contains("2.5")) {
            // Los modelos Gemini 2.5 "piensan", y ese razonamiento consume maxOutputTokens,
            // lo que truncaba el resumen a media frase. Para un resumen corto no hace falta:
            // lo desactivamos (thinkingBudget=0) para que todo el presupuesto sea la respuesta.
            generationConfig.put("thinkingConfig", Map.of("thinkingBudget", 0));
        }

        Map<String, Object> body = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", instruccionSistema + "\n\n" + prompt))
            )),
            "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        HttpEntity<Map<String, Object>> peticion = new HttpEntity<>(body, headers);

        // Reintenta ante errores transitorios (5xx como el 503 "high demand", o cortes
        // de red). Los 4xx (p.ej. 429 por cuota) NO se reintentan: el servicio cae a la
        // plantilla, que es la respuesta correcta para esos casos.
        RuntimeException ultimoError = null;
        for (int intento = 0; intento <= maxReintentos; intento++) {
            try {
                ResponseEntity<String> respuesta =
                    restTemplate.postForEntity(url, peticion, String.class);
                return extraerTexto(respuesta.getBody());
            } catch (HttpServerErrorException | ResourceAccessException e) {
                ultimoError = e;
                log.warn("Gemini no disponible (intento {}/{}): {}",
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

    /** Extrae {@code candidates[0].content.parts[*].text} de la respuesta de Gemini. */
    private String extraerTexto(String json) {
        try {
            JsonNode raiz = objectMapper.readTree(json);
            JsonNode partes = raiz.path("candidates").path(0).path("content").path("parts");
            StringBuilder sb = new StringBuilder();
            for (JsonNode parte : partes) {
                sb.append(parte.path("text").asText(""));
            }
            String texto = sb.toString().trim();
            if (texto.isEmpty()) {
                throw new IllegalStateException("Gemini devolvió una respuesta sin texto");
            }
            return texto;
        } catch (Exception e) {
            log.warn("Respuesta de Gemini no interpretable: {}", e.getMessage());
            throw new RuntimeException("No se pudo interpretar la respuesta de Gemini", e);
        }
    }
}
