package com.redmuqui.platform.proyecto.repository;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {
    Optional<Proyecto> findByCodigoInternoIgnoreCase(String codigoInterno);
    boolean existsByCodigoInternoIgnoreCase(String codigoInterno);
    boolean existsByCodigoInternoIgnoreCaseAndIdNot(String codigoInterno, Long id);
    Optional<Proyecto> findTopByOrderByIdDesc();

    // ----- Agregaciones para el dashboard (RF-069, RF-071, RF-073) -----

    long countByEstado(EstadoProyecto estado);

    List<Proyecto> findByEstado(EstadoProyecto estado);

    @Query("SELECT COALESCE(SUM(p.presupuesto), 0) FROM Proyecto p WHERE p.estado = :estado")
    double sumPresupuestoByEstado(EstadoProyecto estado);

    @Query("SELECT COALESCE(AVG(p.porcentajeAvance), 0) FROM Proyecto p WHERE p.estado = :estado")
    double avgAvanceByEstado(EstadoProyecto estado);

    /** Conteo de proyectos por macroregión; un proyecto cuenta en cada macroregión asociada (N:M). */
    @Query("SELECT m.nombre, COUNT(p) FROM Proyecto p JOIN p.macroregiones m GROUP BY m.nombre ORDER BY COUNT(p) DESC")
    List<Object[]> contarPorMacroregion();

    // ----- Cobertura territorial para el mapa (Sprint 4 ④) -----

    /**
     * Por territorio (id): nº de proyectos y presupuesto total. Un proyecto cuenta
     * en cada territorio asociado (N:M). Sin fan-out: solo se une `territorios`,
     * que es la dimensión de agrupación.
     */
    @Query("SELECT t.id, COUNT(p), COALESCE(SUM(p.presupuesto), 0) " +
           "FROM Proyecto p JOIN p.territorios t GROUP BY t.id")
    List<Object[]> agregarPorTerritorio();

    /** Por territorio (id): nº de instituciones distintas involucradas (consulta aparte para no inflar el presupuesto). */
    @Query("SELECT t.id, COUNT(DISTINCT pi.institucion.id) " +
           "FROM Proyecto p JOIN p.territorios t JOIN p.instituciones pi GROUP BY t.id")
    List<Object[]> contarInstitucionesPorTerritorio();
}
