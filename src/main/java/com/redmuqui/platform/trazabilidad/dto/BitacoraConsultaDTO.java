package com.redmuqui.platform.trazabilidad.dto;

import java.time.LocalDateTime;

/**
 * Vista de consulta de bitácora (RF-066, HU021).
 */
public record BitacoraConsultaDTO(
    String nombre,
    String descripcion,
    String tipoAccion,
    LocalDateTime fecha
) {}
