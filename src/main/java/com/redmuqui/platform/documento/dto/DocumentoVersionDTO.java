package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;

import java.time.LocalDateTime;

public record DocumentoVersionDTO(
    Long id,
    Integer numeroVersion,
    String titulo,
    String descripcion,
    String tipo,
    EstadoDocumento estado,
    String motivoCambio,
    Long idUsuarioCambio,
    String usuarioCambio,
    LocalDateTime fechaCreacion
) {}
