package com.redmuqui.platform.institucion.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.institucion.dto.InstitucionDTO;
import com.redmuqui.platform.institucion.entity.Institucion;
import com.redmuqui.platform.institucion.repository.InstitucionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitucionService {

    private final InstitucionRepository repository;

    @Transactional(readOnly = true)
    public List<InstitucionDTO> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public InstitucionDTO obtener(Long id) { return toDTO(buscarOFallar(id)); }

    @Transactional
    public InstitucionDTO crear(InstitucionDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una institución con el nombre: " + dto.nombre());
        }
        Institucion entity = Institucion.builder().nombre(dto.nombre()).tipo(dto.tipo()).build();
        return toDTO(repository.save(entity));
    }

    @Transactional
    public InstitucionDTO actualizar(Long id, InstitucionDTO dto) {
        Institucion entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setTipo(dto.tipo());
        return toDTO(entity);
    }

    @Transactional
    public void eliminar(Long id) { repository.delete(buscarOFallar(id)); }

    private Institucion buscarOFallar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Institucion", id));
    }

    private InstitucionDTO toDTO(Institucion i) {
        return new InstitucionDTO(i.getId(), i.getNombre(), i.getTipo());
    }
}
