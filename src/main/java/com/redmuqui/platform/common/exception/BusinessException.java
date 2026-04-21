package com.redmuqui.platform.common.exception;

/**
 * Se lanza cuando se viola una regla de negocio del dominio.
 * Mapeada por GlobalExceptionHandler a HTTP 422 (Unprocessable Entity).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
