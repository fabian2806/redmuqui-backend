package com.redmuqui.platform.territorio.service;

import com.redmuqui.platform.common.catalog.service.BaseCatalogoService;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.territorio.dto.TerritorioCreateDTO;
import com.redmuqui.platform.territorio.dto.TerritorioResponseDTO;
import com.redmuqui.platform.territorio.dto.TerritorioUpdateDTO;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import org.springframework.stereotype.Service;

@Service
public class TerritorioServiceImpl extends BaseCatalogoService<Territorio, TerritorioResponseDTO>
        implements TerritorioService {

    private final TerritorioRepository repository;

    public TerritorioServiceImpl(TerritorioRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected TerritorioResponseDTO toDTO(Territorio entity) {
        TerritorioResponseDTO dto = new TerritorioResponseDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setActivo(entity.getActivo());
        return dto;
    }

    @Override
    protected Territorio fromDTO(TerritorioResponseDTO dto) {
        Territorio entity = new Territorio();
        entity.setId(dto.id());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return entity;
    }

    @Override
    protected String getNombreEntidad() {
        return "Territorio";
    }

    @Override
    public TerritorioResponseDTO crear(TerritorioCreateDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new DuplicateResourceException("Ya existe un territorio con el nombre: " + dto.getNombre());
        }
        Territorio entity = new Territorio();
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return toDTO(repository.save(entity));
    }

    @Override
    public TerritorioResponseDTO actualizar(Long id, TerritorioUpdateDTO dto) {
        Territorio entity = buscarOFallar(id);
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return toDTO(entity);
    }
}
