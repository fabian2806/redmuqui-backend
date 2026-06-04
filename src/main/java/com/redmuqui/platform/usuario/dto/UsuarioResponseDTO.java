package com.redmuqui.platform.usuario.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponseDTO(
    Long id,
    String nombres,
    String apellidos,
    String email,
    String telefono,
    Boolean estado,
    String nombreRol,
    Long idRol,
    String nombreInstitucion,
    Long idInstitucion,
    LocalDateTime ultimoAcceso,
    List<String> permisos
) {}
