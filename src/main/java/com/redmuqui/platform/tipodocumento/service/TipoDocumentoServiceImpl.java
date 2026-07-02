package com.redmuqui.platform.tipodocumento.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoCreateDTO;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoResponseDTO;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoUpdateDTO;
import com.redmuqui.platform.tipodocumento.entity.TipoDocumento;
import com.redmuqui.platform.tipodocumento.repository.TipoDocumentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository repository;

    public TipoDocumentoServiceImpl(TipoDocumentoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoResponseDTO> listar() {
        return repository.findByActivoTrueOrderByNombreAsc().stream()
            .map(this::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoDocumentoResponseDTO> listarPaginado(Pageable pageable) {
        return repository.findAllByOrderByNombreAsc(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoDocumentoResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Override
    @Transactional
    public TipoDocumentoResponseDTO crear(TipoDocumentoCreateDTO dto) {
        if (repository.existsByCodigoIgnoreCase(dto.codigo())) {
            throw new DuplicateResourceException("Ya existe un tipo de documento con el código: " + dto.codigo());
        }
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe un tipo de documento con el nombre: " + dto.nombre());
        }
        TipoDocumento entity = new TipoDocumento();
        entity.setNombre(dto.nombre());
        entity.setCodigo(dto.codigo());
        entity.setDescripcion(dto.descripcion());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public TipoDocumentoResponseDTO actualizar(Long id, TipoDocumentoUpdateDTO dto) {
        TipoDocumento entity = buscarOFallar(id);
        if (!entity.getCodigo().equalsIgnoreCase(dto.codigo()) && repository.existsByCodigoIgnoreCase(dto.codigo())) {
            throw new DuplicateResourceException("Ya existe un tipo de documento con el código: " + dto.codigo());
        }
        if (!entity.getNombre().equalsIgnoreCase(dto.nombre()) && repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe un tipo de documento con el nombre: " + dto.nombre());
        }
        entity.setNombre(dto.nombre());
        entity.setCodigo(dto.codigo());
        entity.setDescripcion(dto.descripcion());
        entity.setActivo(dto.activo());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarOFallar(id));
    }

    private TipoDocumento buscarOFallar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento", id));
    }

    private TipoDocumentoResponseDTO toDTO(TipoDocumento entity) {
        return new TipoDocumentoResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getCodigo(),
            entity.getDescripcion(),
            entity.getActivo()
        );
    }
}
