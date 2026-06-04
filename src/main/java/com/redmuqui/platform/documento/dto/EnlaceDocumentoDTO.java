package com.redmuqui.platform.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public record EnlaceDocumentoDTO(
    Long id,
    @NotBlank @URL @Size(max = 1000) String url,
    @NotBlank String descripcion,
    LocalDateTime fechaRegistro,
    Long idDocumento
) {}
