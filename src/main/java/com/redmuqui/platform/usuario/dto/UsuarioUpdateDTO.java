package com.redmuqui.platform.usuario.dto;

import jakarta.validation.constraints.*;

public record UsuarioUpdateDTO(
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Email @Size(max = 150) String email,
    @Size(max = 30) String telefono,
    @NotNull Long idRol,
    Long idInstitucion
) {}
