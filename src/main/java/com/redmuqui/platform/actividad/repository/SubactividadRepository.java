package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Subactividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubactividadRepository extends JpaRepository<Subactividad, Long> {
    List<Subactividad> findByActividadId(Long idActividad);
}
