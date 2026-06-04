package com.redmuqui.platform.documento.dto;

import java.time.LocalDateTime;

public record ArchivoDTO(
    Long id,
    String nombreArchivo,
    String rutaArchivo,
    String tipoArchivo,
    String descripcion,
    Long tamanioArchivo,
    LocalDateTime fechaRegistro,
    Long idDocumento
) {}
