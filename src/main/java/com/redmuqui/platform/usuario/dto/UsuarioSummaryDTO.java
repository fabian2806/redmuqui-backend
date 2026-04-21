package com.redmuqui.platform.usuario.dto;

public record UsuarioSummaryDTO(
    Long id,
    String nombres,
    String apellidos,
    String email
) {
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
