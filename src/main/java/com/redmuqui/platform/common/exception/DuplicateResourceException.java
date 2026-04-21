package com.redmuqui.platform.common.exception;

/**
 * Se lanza cuando se intenta crear un recurso que viola una restricción de unicidad.
 * Mapeada por GlobalExceptionHandler a HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
