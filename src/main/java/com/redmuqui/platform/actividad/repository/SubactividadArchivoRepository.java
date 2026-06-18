package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.SubactividadArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubactividadArchivoRepository extends JpaRepository<SubactividadArchivo, Long> {
    List<SubactividadArchivo> findBySubactividadId(Long idSubactividad);
    Optional<SubactividadArchivo> findByIdAndSubactividadId(Long id, Long subactividadId);
}
