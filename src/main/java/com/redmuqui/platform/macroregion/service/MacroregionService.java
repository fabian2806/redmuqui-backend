package com.redmuqui.platform.macroregion.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.macroregion.dto.MacroregionDTO;
import com.redmuqui.platform.macroregion.entity.Macroregion;
import com.redmuqui.platform.macroregion.repository.MacroregionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MacroregionService {

    private final MacroregionRepository repository;

    @Transactional(readOnly = true)
    public List<MacroregionDTO> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public MacroregionDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Transactional
    public MacroregionDTO crear(MacroregionDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una macroregión con el nombre: " + dto.nombre());
        }
        Macroregion entity = Macroregion.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .build();
        return toDTO(repository.save(entity));
    }

    @Transactional
    public MacroregionDTO actualizar(Long id, MacroregionDTO dto) {
        Macroregion entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(entity);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarOFallar(id));
    }

    private Macroregion buscarOFallar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Macroregion", id));
    }

    private MacroregionDTO toDTO(Macroregion m) {
        return new MacroregionDTO(m.getId(), m.getNombre(), m.getDescripcion());
    }
}
