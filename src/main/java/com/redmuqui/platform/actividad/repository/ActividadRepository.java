package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByProyectoId(Long idProyecto);
    Page<Actividad> findByProyectoId(Long idProyecto, Pageable pageable);
    Page<Actividad> findByEstado(EstadoActividad estado, Pageable pageable);
    List<Actividad> findByHitoIdOrderByFechaInicioAscIdAsc(Long idHito);
    boolean existsByHitoId(Long idHito);
}
