package com.redmuqui.platform.trazabilidad.repository;

import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;
import com.redmuqui.platform.trazabilidad.entity.Observacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {
    List<Observacion> findByEntidadReferenciadaAndIdEntidadRef(
        String entidadReferenciada, Long idEntidadRef);
    Page<Observacion> findByEstado(EstadoObservacion estado, Pageable pageable);
}
