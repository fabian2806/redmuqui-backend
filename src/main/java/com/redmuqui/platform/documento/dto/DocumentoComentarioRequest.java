package com.redmuqui.platform.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentoComentarioRequest(
    @NotBlank @Size(max = 4000) String comentario
) {}
