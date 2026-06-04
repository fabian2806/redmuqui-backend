package com.redmuqui.platform.trazabilidad.repository;

import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {

    /** RF-064: auditoría general, más reciente primero. */
    @EntityGraph(attributePaths = "usuario")
    Page<Bitacora> findAllByOrderByFechaDesc(Pageable pageable);

    /** RF-065: historial de una entidad polimórfica, más reciente primero. */
    @EntityGraph(attributePaths = "usuario")
    Page<Bitacora> findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc(
        String entidadReferenciada, Long idEntidadRef, Pageable pageable);

    Page<Bitacora> findByUsuarioId(Long idUsuario, Pageable pageable);
}
