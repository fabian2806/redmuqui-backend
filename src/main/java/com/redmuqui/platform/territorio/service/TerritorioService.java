package com.redmuqui.platform.territorio.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.territorio.dto.TerritorioDTO;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TerritorioService {

    private final TerritorioRepository repository;

    @Transactional(readOnly = true)
    public List<TerritorioDTO> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public TerritorioDTO obtener(Long id) { return toDTO(buscarOFallar(id)); }

    @Transactional
    public TerritorioDTO crear(TerritorioDTO dto) {
        Territorio entity = Territorio.builder().nombre(dto.nombre()).descripcion(dto.descripcion()).build();
        return toDTO(repository.save(entity));
    }

    @Transactional
    public TerritorioDTO actualizar(Long id, TerritorioDTO dto) {
        Territorio entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(entity);
    }

    @Transactional
    public void eliminar(Long id) { repository.delete(buscarOFallar(id)); }

    private Territorio buscarOFallar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Territorio", id));
    }

    private TerritorioDTO toDTO(Territorio t) {
        return new TerritorioDTO(t.getId(), t.getNombre(), t.getDescripcion());
    }
}
