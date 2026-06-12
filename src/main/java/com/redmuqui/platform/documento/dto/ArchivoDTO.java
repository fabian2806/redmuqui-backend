package com.redmuqui.platform.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArchivoDTO(
    Long id,
    @NotBlank @Size(max = 255) String nombre,
    @NotBlank @Size(max = 500) String url,
    @Size(max = 20) String extension,
    Long idDocumento
) {}
