package com.redmuqui.platform.usuario.mapper;

import com.redmuqui.platform.usuario.dto.UsuarioResponseDTO;
import com.redmuqui.platform.usuario.dto.UsuarioSummaryDTO;
import com.redmuqui.platform.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponseDTO(Usuario u) {
        return new UsuarioResponseDTO(
            u.getId(),
            u.getNombres(),
            u.getApellidos(),
            u.getEmail(),
            u.getTelefono(),
            u.getEstado(),
            u.getRol() != null ? u.getRol().getNombre() : null,
            u.getRol() != null ? u.getRol().getId() : null,
            u.getInstitucion() != null ? u.getInstitucion().getNombre() : null,
            u.getInstitucion() != null ? u.getInstitucion().getId() : null,
            u.getUltimoAcceso()
        );
    }

    public UsuarioSummaryDTO toSummaryDTO(Usuario u) {
        return new UsuarioSummaryDTO(u.getId(), u.getNombres(), u.getApellidos(), u.getEmail());
    }
}
