package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Fase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaseRepository extends JpaRepository<Fase, Long> {
    List<Fase> findByProyectoIdOrderByFechaInicioPlanificadaAscIdAsc(Long proyectoId);
    Optional<Fase> findByIdAndProyectoId(Long id, Long proyectoId);
}
