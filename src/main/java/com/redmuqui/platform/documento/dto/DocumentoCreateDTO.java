package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record DocumentoCreateDTO(
    @NotBlank String titulo,
    String descripcion,
    String tipo,
    EstadoDocumento estado,
    String tipoArchivo,
    String enlace,
    Long idProyecto,
    Long idEjeTematico,
    @NotNull Long idRespElaboracion,
    Long idRespValidacion,
    Set<Long> idTerritorios
) {}
