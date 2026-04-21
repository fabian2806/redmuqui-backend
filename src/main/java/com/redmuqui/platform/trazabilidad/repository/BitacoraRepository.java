package com.redmuqui.platform.trazabilidad.repository;

import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {
    Page<Bitacora> findByEntidadReferenciadaAndIdEntidadRef(
        String entidadReferenciada, Long idEntidadRef, Pageable pageable);
    Page<Bitacora> findByUsuarioId(Long idUsuario, Pageable pageable);
}
