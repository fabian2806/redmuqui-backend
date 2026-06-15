package com.redmuqui.platform.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiLlmClient(
            @Value("${app.ia.api-key:}") String apiKey,
            @Value("${app.ia.model:gemini-2.0-flash}") String modelo,
            @Value("${app.ia.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ia.timeout-ms:30000}") int timeoutMs) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelo = modelo;
        this.baseUrl = baseUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
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

        Map<String, Object> body = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", instruccionSistema + "\n\n" + prompt))
            )),
            "generationConfig", Map.of(
                "temperature", 0.4,
                "maxOutputTokens", 800
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        ResponseEntity<String> respuesta =
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        return extraerTexto(respuesta.getBody());
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
