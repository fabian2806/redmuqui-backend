package com.redmuqui.platform.rol.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.rol.dto.PermisoDTO;
import com.redmuqui.platform.rol.dto.RolDTO;
import com.redmuqui.platform.rol.entity.Rol;
import com.redmuqui.platform.rol.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository repository;

    @Transactional(readOnly = true)
    public List<RolDTO> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public RolDTO obtener(Long id) {
        return toDTO(repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rol", id)));
    }

    @Transactional(readOnly = true)
    public List<PermisoDTO> obtenerPermisos(Long idRol) {
        Rol rol = repository.findById(idRol)
            .orElseThrow(() -> new ResourceNotFoundException("Rol", idRol));
        return rol.getPermisos().stream()
            .map(p -> new PermisoDTO(p.getId(), p.getNombre(), p.getTipo()))
            .toList();
    }

    private RolDTO toDTO(Rol r) {
        return new RolDTO(
            r.getId(),
            r.getNombre(),
            r.getDescripcion(),
            r.getPermisos().stream()
                .map(p -> new PermisoDTO(p.getId(), p.getNombre(), p.getTipo()))
                .collect(Collectors.toSet())
        );
    }
}
