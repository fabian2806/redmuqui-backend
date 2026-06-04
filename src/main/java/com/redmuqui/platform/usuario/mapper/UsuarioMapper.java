package com.redmuqui.platform.usuario.mapper;

import com.redmuqui.platform.usuario.dto.UsuarioResponseDTO;
import com.redmuqui.platform.usuario.dto.UsuarioSummaryDTO;
import com.redmuqui.platform.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponseDTO(Usuario u) {
        List<String> permisos = u.getRol() != null
            ? u.getRol().getPermisos().stream()
                .map(permiso -> permiso.getNombre())
                .sorted()
                .toList()
            : List.of();

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
            u.getUltimoAcceso(),
            permisos
        );
    }

    public UsuarioSummaryDTO toSummaryDTO(Usuario u) {
        return new UsuarioSummaryDTO(u.getId(), u.getNombres(), u.getApellidos(), u.getEmail());
    }
}
