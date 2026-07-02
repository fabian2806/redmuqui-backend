package com.redmuqui.platform.moneda.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.moneda.dto.MonedaCreateDTO;
import com.redmuqui.platform.moneda.dto.MonedaResponseDTO;
import com.redmuqui.platform.moneda.dto.MonedaUpdateDTO;
import com.redmuqui.platform.moneda.entity.Moneda;
import com.redmuqui.platform.moneda.repository.MonedaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MonedaServiceImpl implements MonedaService {

    private final MonedaRepository repository;

    public MonedaServiceImpl(MonedaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonedaResponseDTO> listar() {
        return repository.findByActivoTrueOrderByNombreAsc().stream()
            .map(this::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonedaResponseDTO> listarPaginado(Pageable pageable) {
        return repository.findAllByOrderByNombreAsc(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MonedaResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Override
    @Transactional
    public MonedaResponseDTO crear(MonedaCreateDTO dto) {
        if (repository.existsByCodigoIgnoreCase(dto.codigo())) {
            throw new DuplicateResourceException("Ya existe una moneda con el código: " + dto.codigo());
        }
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una moneda con el nombre: " + dto.nombre());
        }
        Moneda entity = new Moneda();
        entity.setNombre(dto.nombre());
        entity.setCodigo(dto.codigo());
        entity.setSimbolo(dto.simbolo());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public MonedaResponseDTO actualizar(Long id, MonedaUpdateDTO dto) {
        Moneda entity = buscarOFallar(id);
        if (!entity.getCodigo().equalsIgnoreCase(dto.codigo()) && repository.existsByCodigoIgnoreCase(dto.codigo())) {
            throw new DuplicateResourceException("Ya existe una moneda con el código: " + dto.codigo());
        }
        if (!entity.getNombre().equalsIgnoreCase(dto.nombre()) && repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una moneda con el nombre: " + dto.nombre());
        }
        entity.setNombre(dto.nombre());
        entity.setCodigo(dto.codigo());
        entity.setSimbolo(dto.simbolo());
        entity.setActivo(dto.activo());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarOFallar(id));
    }

    private Moneda buscarOFallar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Moneda", id));
    }

    private MonedaResponseDTO toDTO(Moneda entity) {
        return new MonedaResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getCodigo(),
            entity.getSimbolo(),
            entity.getActivo()
        );
    }
}
