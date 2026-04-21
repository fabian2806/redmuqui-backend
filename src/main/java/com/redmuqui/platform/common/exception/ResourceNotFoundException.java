package com.redmuqui.platform.common.exception;

/**
 * Se lanza cuando se intenta acceder a un recurso que no existe.
 * Mapeada por GlobalExceptionHandler a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s con id %s no encontrado", resourceName, id));
    }
}
