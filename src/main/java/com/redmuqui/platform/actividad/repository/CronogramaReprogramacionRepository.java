package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.CronogramaReprogramacion;
import com.redmuqui.platform.actividad.entity.TipoEntidadCronograma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CronogramaReprogramacionRepository extends JpaRepository<CronogramaReprogramacion, Long> {
    List<CronogramaReprogramacion> findByTipoEntidadAndIdEntidadOrderByFechaCreacionDesc(
        TipoEntidadCronograma tipoEntidad,
        Long idEntidad
    );
}
