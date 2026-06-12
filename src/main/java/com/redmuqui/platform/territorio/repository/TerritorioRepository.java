package com.redmuqui.platform.territorio.repository;

import com.redmuqui.platform.common.catalog.repository.BaseCatalogoRepository;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.entity.TipoTerritorio;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerritorioRepository extends BaseCatalogoRepository<Territorio> {

    /**
     * Unidades de un nivel CON código UBIGEO, para el mapa territorial.
     * Excluye territorios de texto libre (sin código) como zonas o cuencas,
     * que no son una unidad geográfica del mapa.
     */
    List<Territorio> findByTipoAndCodigoNotNullOrderByNombreAsc(TipoTerritorio tipo);
}
