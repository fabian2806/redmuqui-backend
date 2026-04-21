package com.redmuqui.platform.trazabilidad.dto;

import java.time.LocalDateTime;

public record BitacoraResponseDTO(
    Long id,
    String tipoAccion,
    LocalDateTime fecha,
    String descripcion,
    String entidadReferenciada,
    Long idEntidadRef,
    Long idUsuario,
    String nombreUsuario
) {}
