package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;

import java.time.LocalDate;
import java.util.Set;

public record DocumentoResponseDTO(
    Long id,
    String titulo,
    String descripcion,
    String tipo,
    EstadoDocumento estado,
    String tipoArchivo,
    LocalDate fechaCarga,
    String enlace,
    Double version,
    Long idProyecto,
    Long idEjeTematico,
    Long idRespElaboracion,
    Long idRespValidacion,
    Set<Long> idTerritorios
) {}
