package com.redmuqui.platform.common.catalog.service;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;
import com.redmuqui.platform.common.catalog.entity.BaseCatalogo;
import com.redmuqui.platform.common.catalog.repository.BaseCatalogoRepository;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseCatalogoService<T extends BaseCatalogo, D extends BaseCatalogoDTO> {

    protected final BaseCatalogoRepository<T> repository;

    protected BaseCatalogoService(BaseCatalogoRepository<T> repository) {
        this.repository = repository;
    }

    protected abstract D toDTO(T entity);

    protected abstract T fromDTO(D dto);

    protected abstract String getNombreEntidad();

    @Transactional(readOnly = true)
    public List<D> listar() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public D obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Transactional
    public D crear(D dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe " + getNombreEntidad() + " con el nombre: " + dto.nombre());
        }
        return toDTO(repository.save(fromDTO(dto)));
    }

    @Transactional
    public D actualizar(Long id, D dto) {
        T entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(entity);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarOFallar(id));
    }

    protected T buscarOFallar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(getNombreEntidad(), id));
    }
}
