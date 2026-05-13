package com.redmuqui.platform.ejetematico.service;

import com.redmuqui.platform.common.catalog.service.BaseCatalogoService;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoCreateDTO;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoResponseDTO;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoUpdateDTO;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import org.springframework.stereotype.Service;

@Service
public class EjeTematicoServiceImpl extends BaseCatalogoService<EjeTematico, EjeTematicoResponseDTO>
        implements EjeTematicoService {

    private final EjeTematicoRepository repository;

    public EjeTematicoServiceImpl(EjeTematicoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected EjeTematicoResponseDTO toDTO(EjeTematico entity) {
        return new EjeTematicoResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getDescripcion()
        );
    }

    @Override
    protected EjeTematico fromDTO(EjeTematicoResponseDTO dto) {
        EjeTematico entity = new EjeTematico();
        entity.setId(dto.id());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return entity;
    }

    @Override
    protected String getNombreEntidad() {
        return "EjeTematico";
    }

    @Override
    public EjeTematicoResponseDTO crear(EjeTematicoCreateDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe un eje temático con el nombre: " + dto.nombre());
        }
        EjeTematico entity = new EjeTematico();
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(repository.save(entity));
    }

    @Override
    public EjeTematicoResponseDTO actualizar(Long id, EjeTematicoUpdateDTO dto) {
        EjeTematico entity = buscarOFallar(id);
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        return toDTO(entity);
    }
}
