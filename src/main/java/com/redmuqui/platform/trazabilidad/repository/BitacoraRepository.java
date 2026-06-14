package com.redmuqui.platform.trazabilidad.repository;

import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

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

    @EntityGraph(attributePaths = "usuario")
    @Query("""
        SELECT b FROM Bitacora b
        WHERE LOWER(COALESCE(b.descripcion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.entidadReferenciada, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.tipoAccion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.usuario.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.usuario.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY b.fecha DESC
        """)
    Page<Bitacora> buscar(String q, Pageable pageable);

    @EntityGraph(attributePaths = "usuario")
    @Query("""
        SELECT b FROM Bitacora b
        WHERE b.entidadReferenciada = :entidad
          AND b.idEntidadRef = :idEntidad
          AND (
            LOWER(COALESCE(b.descripcion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(b.tipoAccion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(b.usuario.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(COALESCE(b.usuario.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY b.fecha DESC
        """)
    Page<Bitacora> buscarEntidad(
        String entidad, Long idEntidad, String q, Pageable pageable);
}
