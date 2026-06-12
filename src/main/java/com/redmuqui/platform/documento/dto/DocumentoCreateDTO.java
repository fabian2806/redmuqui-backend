package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DocumentoCreateDTO(
    @NotBlank @Size(max = 255) String titulo,
    String descripcion,
    @NotBlank @Size(max = 100) String tipo,
    EstadoDocumento estado,
    @Size(max = 50) String tipoArchivo,
    @Size(max = 500) String enlace,
    Long idProyecto,
    Long idEjeTematico,
    @NotNull Long idRespElaboracion,
    Long idRespValidacion,
    Set<Long> idTerritorios
) {}
