package com.redmuqui.platform.documento.dto;

import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.entity.TipoVinculoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record DocumentoUpdateDTO(
    @NotBlank @Size(max = 255) String titulo,
    String descripcion,
    @NotBlank @Size(max = 100) String tipo,
    @NotNull EstadoDocumento estado,
    @Size(max = 50) String tipoArchivo,
    @Size(max = 500) String enlace,
    @NotNull LocalDate fechaCarga,
    Long idProyecto,
    Long idSubactividad,
    TipoVinculoDocumento tipoVinculo,
    Long idEjeTematico,
    @NotNull Long idRespElaboracion,
    Long idRespValidacion,
    Set<Long> idTerritorios
) {}
