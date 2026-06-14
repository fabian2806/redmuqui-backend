package com.redmuqui.platform.documento.dto;

import java.time.LocalDateTime;

public record DocumentoComentarioDTO(
    Long id,
    String comentario,
    Long idUsuario,
    String usuario,
    LocalDateTime fechaCreacion
) {}
