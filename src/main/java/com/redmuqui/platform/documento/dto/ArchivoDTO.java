package com.redmuqui.platform.documento.dto;

import jakarta.validation.constraints.NotBlank;

public record ArchivoDTO(
    Long id,
    @NotBlank String nombre,
    @NotBlank String url,
    String extension,
    Long idDocumento
) {}
