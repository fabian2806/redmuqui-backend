package com.redmuqui.platform.reporte.dto;

import java.time.LocalDateTime;

/**
 * Evento reciente para reportes, derivado de bitacora.
 */
public record ActividadRecienteDTO(
    String usuario,
    String descripcion,
    String tipoAccion,
    String entidadReferenciada,
    LocalDateTime fecha
) {}
