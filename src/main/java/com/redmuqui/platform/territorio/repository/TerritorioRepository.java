package com.redmuqui.platform.territorio.repository;

import com.redmuqui.platform.common.catalog.repository.BaseCatalogoRepository;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.entity.TipoTerritorio;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerritorioRepository extends BaseCatalogoRepository<Territorio> {

    /** Unidades de un nivel (p. ej. todos los departamentos) para el mapa territorial. */
    List<Territorio> findByTipoOrderByNombreAsc(TipoTerritorio tipo);
}
