package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByProyectoId(Long idProyecto);
    Page<Actividad> findByProyectoId(Long idProyecto, Pageable pageable);
    Page<Actividad> findByEstado(EstadoActividad estado, Pageable pageable);
    List<Actividad> findByHitoIdOrderByFechaInicioAscIdAsc(Long idHito);
    boolean existsByHitoId(Long idHito);

    // ----- Agregaciones para el gráfico de estado de actividades (RF-074) -----

    long countByEstado(EstadoActividad estado);

    /** Actividades no finalizadas cuya fecha de fin ya pasó (estado "vencida" derivado). */
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.fechaFin < :hoy AND a.estado <> com.redmuqui.platform.actividad.entity.EstadoActividad.FINALIZADA")
    long countVencidas(LocalDate hoy);

    /** Actividades en un estado dado que aún están vigentes (sin fecha de fin o no vencidas). */
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.estado = :estado AND (a.fechaFin IS NULL OR a.fechaFin >= :hoy)")
    long countVigentesByEstado(EstadoActividad estado, LocalDate hoy);
}
