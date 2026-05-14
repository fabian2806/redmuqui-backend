package com.redmuqui.platform.macroregion.service;

import com.redmuqui.platform.common.catalog.service.BaseCatalogoService;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.macroregion.dto.MacroregionCreateDTO;
import com.redmuqui.platform.macroregion.dto.MacroregionResponseDTO;
import com.redmuqui.platform.macroregion.dto.MacroregionUpdateDTO;
import com.redmuqui.platform.macroregion.entity.Macroregion;
import com.redmuqui.platform.macroregion.repository.MacroregionRepository;
import org.springframework.stereotype.Service;

@Service
public class MacroregionServiceImpl extends BaseCatalogoService<Macroregion, MacroregionResponseDTO> implements MacroregionService {

    private final MacroregionRepository repository;

    public MacroregionServiceImpl(MacroregionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected MacroregionResponseDTO toDTO(Macroregion entity) {
        return new MacroregionResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getDescripcion()
        );
    }

    @Override
    protected Macroregion fromDTO(MacroregionResponseDTO dto) {
        Macroregion entity = new Macroregion();
        entity.setId(dto.id());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return entity;
    }

    @Override
    protected String getNombreEntidad() {
        return "Macroregion";
    }

    @Override
    public MacroregionResponseDTO crear(MacroregionCreateDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una macroregión con el nombre: " + dto.nombre());
        }
        Macroregion entity = new Macroregion();
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(repository.save(entity));
    }

    @Override
    public MacroregionResponseDTO actualizar(Long id, MacroregionUpdateDTO dto) {
        Macroregion entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(entity);
    }
}