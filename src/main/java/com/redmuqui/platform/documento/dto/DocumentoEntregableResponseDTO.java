package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;

import java.time.LocalDate;

public record DocumentoEntregableResponseDTO(
    Long id,
    String titulo,
    EstadoDocumento estado,
    Double version,
    LocalDate fechaCarga,
    String usuarioCarga
) {}
