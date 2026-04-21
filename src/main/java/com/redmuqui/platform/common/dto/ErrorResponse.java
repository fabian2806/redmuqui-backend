package com.redmuqui.platform.common.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura estándar de respuesta de error de la API.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldError> fieldErrors
) {

    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        this(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }

    public record FieldError(String field, String message) {}
}
