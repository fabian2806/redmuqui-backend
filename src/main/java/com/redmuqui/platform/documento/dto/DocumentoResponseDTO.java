package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.entity.TipoVinculoDocumento;

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
    Long idSubactividad,
    String nombreSubactividad,
    Long idActividad,
    TipoVinculoDocumento tipoVinculo,
    Long idEjeTematico,
    Long idRespElaboracion,
    Long idRespValidacion,
    Set<Long> idTerritorios,
    Long idUsuarioCarga,
    String usuarioCarga
) {}
