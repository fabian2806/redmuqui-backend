package com.redmuqui.platform.estado.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.estado.dto.EstadoCreateDTO;
import com.redmuqui.platform.estado.dto.EstadoResponseDTO;
import com.redmuqui.platform.estado.dto.EstadoUpdateDTO;
import com.redmuqui.platform.estado.entity.Estado;
import com.redmuqui.platform.estado.entity.ModuloEstado;
import com.redmuqui.platform.estado.repository.EstadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstadoServiceImpl implements EstadoService {

    private final EstadoRepository repository;

    public EstadoServiceImpl(EstadoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoResponseDTO> listar(ModuloEstado modulo) {
        return repository.findByModuloAndActivoTrueOrderByNombreAsc(modulo).stream()
            .map(this::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstadoResponseDTO> listarPaginado(ModuloEstado modulo, Pageable pageable) {
        return repository.findByModuloOrderByNombreAsc(modulo, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Override
    @Transactional
    public EstadoResponseDTO crear(EstadoCreateDTO dto) {
        if (repository.existsByCodigoIgnoreCaseAndModulo(dto.codigo(), dto.modulo())) {
            throw new DuplicateResourceException("Ya existe un estado con el código '" + dto.codigo() + "' en el módulo " + dto.modulo());
        }
        if (repository.existsByNombreIgnoreCaseAndModulo(dto.nombre(), dto.modulo())) {
            throw new DuplicateResourceException("Ya existe un estado con el nombre '" + dto.nombre() + "' en el módulo " + dto.modulo());
        }
        Estado entity = new Estado();
        entity.setNombre(dto.nombre());
        entity.setCodigo(dto.codigo());
        entity.setDescripcion(dto.descripcion());
        entity.setModulo(dto.modulo());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public EstadoResponseDTO actualizar(Long id, EstadoUpdateDTO dto) {
        Estado entity = buscarOFallar(id);
        if (!entity.getCodigo().equalsIgnoreCase(dto.codigo()) && repository.existsByCodigoIgnoreCaseAndModulo(dto.codigo(), dto.modulo())) {
            throw new DuplicateResourceException("Ya existe un estado con el código '" + dto.codigo() + "' en el módulo " + dto.modulo());
        }
        if (!entity.getNombre().equalsIgnoreCase(dto.nombre()) && repository.existsByNombreIgnoreCaseAndModulo(dto.nombre(), dto.modulo())) {
            throw new DuplicateResourceException("Ya existe un estado con el nombre '" + dto.nombre() + "' en el módulo " + dto.modulo());
        }
        entity.setNombre(dto.nombre());
        entity.setCodigo(dto.codigo());
        entity.setDescripcion(dto.descripcion());
        entity.setModulo(dto.modulo());
        entity.setActivo(dto.activo());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarOFallar(id));
    }

    private Estado buscarOFallar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Estado", id));
    }

    private EstadoResponseDTO toDTO(Estado entity) {
        return new EstadoResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getCodigo(),
            entity.getDescripcion(),
            entity.getModulo(),
            entity.getActivo()
        );
    }
}
