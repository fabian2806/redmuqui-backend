package com.redmuqui.platform.ejetematico.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoDTO;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EjeTematicoService {

    private final EjeTematicoRepository repository;

    @Transactional(readOnly = true)
    public List<EjeTematicoDTO> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public EjeTematicoDTO obtener(Long id) { return toDTO(buscarOFallar(id)); }

    @Transactional
    public EjeTematicoDTO crear(EjeTematicoDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe un eje temático con el nombre: " + dto.nombre());
        }
        EjeTematico entity = EjeTematico.builder().nombre(dto.nombre()).descripcion(dto.descripcion()).build();
        return toDTO(repository.save(entity));
    }

    @Transactional
    public EjeTematicoDTO actualizar(Long id, EjeTematicoDTO dto) {
        EjeTematico entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(entity);
    }

    @Transactional
    public void eliminar(Long id) { repository.delete(buscarOFallar(id)); }

    private EjeTematico buscarOFallar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("EjeTematico", id));
    }

    private EjeTematicoDTO toDTO(EjeTematico e) {
        return new EjeTematicoDTO(e.getId(), e.getNombre(), e.getDescripcion());
    }
}
