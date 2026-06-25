package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.SubactividadCofinanciamiento;
import com.redmuqui.platform.actividad.entity.SubactividadCofinanciamientoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubactividadCofinanciamientoRepository extends JpaRepository<SubactividadCofinanciamiento, SubactividadCofinanciamientoId> {
    List<SubactividadCofinanciamiento> findBySubactividad_Id(Long idSubactividad);

    List<SubactividadCofinanciamiento> findByActividadOrigen_Id(Long actividadOrigenId);

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM SubactividadCofinanciamiento c WHERE c.actividadOrigen.id = :actividadId")
    double sumMontoByActividadOrigenId(Long actividadId);
}
