package com.redmuqui.platform.documento.repository;

import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    Page<Documento> findByProyectoId(Long idProyecto, Pageable pageable);
    Page<Documento> findByEstado(EstadoDocumento estado, Pageable pageable);
    Page<Documento> findByEjeTematicoId(Long idEjeTematico, Pageable pageable);

    // ----- Agregaciones para el dashboard (RF-069, RF-072) -----

    long countByEstado(EstadoDocumento estado);

    long countByEstadoIn(java.util.Collection<EstadoDocumento> estados);

    java.util.List<Documento> findTop5ByOrderByFechaCreacionDescIdDesc();
}
