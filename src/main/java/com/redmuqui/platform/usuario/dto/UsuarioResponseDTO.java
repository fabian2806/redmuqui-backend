package com.redmuqui.platform.usuario.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
    Long id,
    String nombres,
    String apellidos,
    String email,
    Boolean estado,
    String nombreRol,
    Long idRol,
    String nombreMacroregion,
    Long idMacroregion,
    String nombreInstitucion,
    Long idInstitucion,
    LocalDateTime ultimoAcceso
) {}
