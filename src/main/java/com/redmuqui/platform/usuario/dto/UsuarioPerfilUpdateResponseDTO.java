package com.redmuqui.platform.usuario.dto;

import com.redmuqui.platform.auth.dto.TokenResponse;

public record UsuarioPerfilUpdateResponseDTO(
    UsuarioResponseDTO usuario,
    TokenResponse tokens
) {}
