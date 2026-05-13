package com.redmuqui.platform.institucion.service;

import com.redmuqui.platform.common.catalog.service.BaseCatalogoService;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.institucion.dto.InstitucionCreateDTO;
import com.redmuqui.platform.institucion.dto.InstitucionResponseDTO;
import com.redmuqui.platform.institucion.dto.InstitucionUpdateDTO;
import com.redmuqui.platform.institucion.entity.Institucion;
import com.redmuqui.platform.institucion.repository.InstitucionRepository;
import org.springframework.stereotype.Service;

@Service
public class InstitucionServiceImpl extends BaseCatalogoService<Institucion, InstitucionResponseDTO>
        implements InstitucionService {

    private final InstitucionRepository repository;

    public InstitucionServiceImpl(InstitucionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected InstitucionResponseDTO toDTO(Institucion entity) {
        return new InstitucionResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getDescripcion(),
            entity.getTipo()
        );
    }

    @Override
    protected Institucion fromDTO(InstitucionResponseDTO dto) {
        Institucion entity = new Institucion();
        entity.setId(dto.id());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setTipo(dto.tipo());
        return entity;
    }

    @Override
    protected String getNombreEntidad() {
        return "Institucion";
    }

    // Override base actualizar to also persist the tipo field
    @Override
    public InstitucionResponseDTO actualizar(Long id, InstitucionResponseDTO dto) {
        Institucion entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setTipo(dto.tipo());
        return toDTO(entity);
    }

    @Override
    public InstitucionResponseDTO crear(InstitucionCreateDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una institución con el nombre: " + dto.nombre());
        }
        Institucion entity = new Institucion();
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setTipo(dto.tipo());
        return toDTO(repository.save(entity));
    }

    @Override
    public InstitucionResponseDTO actualizar(Long id, InstitucionUpdateDTO dto) {
        Institucion entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setTipo(dto.tipo());
        return toDTO(entity);
    }
}
