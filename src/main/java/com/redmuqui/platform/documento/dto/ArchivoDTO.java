package com.redmuqui.platform.documento.dto;

import jakarta.validation.constraints.Size;

public record ArchivoDTO(
        Long id,
        @Size(max = 255) String nombre,
        @Size(max = 1000) String url,
        @Size(max = 20) String extension,
        @Size(max = 150) String tipoContenido,
        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres.")
        String descripcion,
        Long tamanioBytes,
        Integer numeroVersion,
        Long idUsuarioCarga,
        String usuarioCarga
) {
}
