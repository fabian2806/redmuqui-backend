package com.redmuqui.platform.usuario.dto;

import jakarta.validation.constraints.*;

public record UsuarioCreateDTO(
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Email @Size(max = 150) String email,
    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
        message = "Debe incluir mayúscula, minúscula, número y carácter especial"
    )
    String contrasenha,
    @NotNull Long idRol,
    Long idMacroregion,
    Long idInstitucion
) {}
