package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.SubactividadArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubactividadArchivoRepository extends JpaRepository<SubactividadArchivo, Long> {
    List<SubactividadArchivo> findBySubactividadId(Long idSubactividad);
}
