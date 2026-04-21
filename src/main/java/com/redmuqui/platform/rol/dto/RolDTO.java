package com.redmuqui.platform.rol.dto;

import java.util.Set;

public record RolDTO(
    Long id,
    String nombre,
    String descripcion,
    Set<PermisoDTO> permisos
) {}
