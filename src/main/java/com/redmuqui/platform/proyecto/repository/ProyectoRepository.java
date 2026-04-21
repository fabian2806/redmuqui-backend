package com.redmuqui.platform.proyecto.repository;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    Optional<Proyecto> findByCodigoInternoIgnoreCase(String codigoInterno);
    boolean existsByCodigoInternoIgnoreCase(String codigoInterno);
    Page<Proyecto> findByEstado(EstadoProyecto estado, Pageable pageable);
    Page<Proyecto> findByMacroregionId(Long idMacroregion, Pageable pageable);
}
