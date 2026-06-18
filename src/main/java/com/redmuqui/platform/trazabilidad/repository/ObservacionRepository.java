package com.redmuqui.platform.trazabilidad.repository;

import com.redmuqui.platform.trazabilidad.entity.Observacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    // Trae el usuario en la misma query (evita N+1 al resolver su nombre en
    // toDTO), igual que BitacoraRepository.
    @EntityGraph(attributePaths = "usuario")
    Page<Observacion> findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc(
        String entidadReferenciada, Long idEntidadRef, Pageable pageable);
}
