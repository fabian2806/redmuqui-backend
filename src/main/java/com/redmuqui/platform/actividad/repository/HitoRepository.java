package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Hito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HitoRepository extends JpaRepository<Hito, Long> {
    List<Hito> findByProyectoIdOrderByFechaClaveAscIdAsc(Long idProyecto);
    List<Hito> findByFaseIdOrderByFechaClaveAscIdAsc(Long idFase);

    Optional<Hito> findByIdAndProyectoId(Long id, Long idProyecto);

    /**
     * Hitos vencidos (fecha clave pasada y no finalizados) agrupados por proyecto,
     * para clasificar proyectos en riesgo en el dashboard (RF-071).
     * Cada fila: [idProyecto, cantidadHitosVencidos].
     */
    @Query("SELECT h.proyecto.id, COUNT(h) FROM Hito h "
        + "WHERE h.fechaClave < :hoy AND h.estado <> com.redmuqui.platform.actividad.entity.EstadoHito.FINALIZADO "
        + "GROUP BY h.proyecto.id")
    List<Object[]> contarHitosVencidosPorProyecto(LocalDate hoy);
}
