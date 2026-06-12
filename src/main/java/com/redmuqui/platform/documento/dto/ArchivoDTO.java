package com.redmuqui.platform.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArchivoDTO(
        Long id,

        String nombre,

        String url,

        String extension,

        String tipoContenido,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres.")
        String descripcion,

        Long tamanioBytes
) {
}