package com.redmuqui.platform.reporte.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;

import java.time.LocalDate;

/**
 * Documento registrado recientemente, para el panel del dashboard (RF-072).
 */
public record DocumentoRecienteDTO(
    Long id,
    String titulo,
    String tipo,
    EstadoDocumento estado,
    LocalDate fechaCarga
) {}
