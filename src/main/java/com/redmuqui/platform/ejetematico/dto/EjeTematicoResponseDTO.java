package com.redmuqui.platform.ejetematico.dto;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;

public record EjeTematicoResponseDTO(
    Long id,
    String nombre,
    String descripcion
) implements BaseCatalogoDTO {}
