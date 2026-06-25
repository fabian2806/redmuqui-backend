package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByProyectoId(Long idProyecto);
    List<Actividad> findByFaseIdOrderByFechaInicioPlanificadaAscIdAsc(Long idFase);
    Page<Actividad> findByProyectoId(Long idProyecto, Pageable pageable);
    Page<Actividad> findByEstado(EstadoActividad estado, Pageable pageable);
    List<Actividad> findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(Long idHito);
    boolean existsByHitoId(Long idHito);
    boolean existsByFaseId(Long idFase);

    @Query("SELECT COALESCE(SUM(a.presupuesto), 0) FROM Actividad a WHERE a.proyecto.id = :proyectoId")
    double sumPresupuestoByProyectoId(@Param("proyectoId") Long proyectoId);

    @Query("""
        SELECT COALESCE(SUM(a.presupuesto), 0)
        FROM Actividad a
        WHERE a.proyecto.id = :proyectoId
          AND a.id <> :actividadId
        """)
    double sumPresupuestoByProyectoIdExcludingActividad(
        @Param("proyectoId") Long proyectoId,
        @Param("actividadId") Long actividadId
    );

    // ----- Agregaciones para el gráfico de estado de actividades (RF-074) -----

    long countByEstado(EstadoActividad estado);

    /** Actividades no finalizadas cuya fecha de fin ya pasó (estado "vencida" derivado). */
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.fechaFinPlanificada < :hoy AND a.estado <> com.redmuqui.platform.actividad.entity.EstadoActividad.FINALIZADA")
    long countVencidas(LocalDate hoy);

    /** Actividades en un estado dado que aún están vigentes (sin fecha de fin o no vencidas). */
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.estado = :estado AND (a.fechaFinPlanificada IS NULL OR a.fechaFinPlanificada >= :hoy)")
    long countVigentesByEstado(EstadoActividad estado, LocalDate hoy);

    @Query("""
        SELECT a,
               COALESCE((SELECT SUM(s.presupuesto) FROM Subactividad s WHERE s.actividad = a), 0),
               COALESCE((SELECT SUM(c.monto) FROM SubactividadCofinanciamiento c WHERE c.actividadOrigen = a), 0)
        FROM Actividad a
        JOIN a.proyecto p
        WHERE p.moneda = :moneda
          AND (:excludeProyectoId IS NULL OR p.id <> :excludeProyectoId)
        ORDER BY p.nombre ASC, a.nombre ASC
        """)
    List<Object[]> findCandidatasCofinanciamiento(
        @Param("moneda") String moneda,
        @Param("excludeProyectoId") Long excludeProyectoId
    );
}
