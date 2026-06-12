package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Subactividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubactividadRepository extends JpaRepository<Subactividad, Long> {
    List<Subactividad> findByActividadId(Long idActividad);

    // ----- Beneficiarios desagregados por género para el dashboard (RF-069) -----

    @Query("SELECT COALESCE(SUM(s.hombresInvolucrados), 0) FROM Subactividad s")
    long sumHombresInvolucrados();

    @Query("SELECT COALESCE(SUM(s.mujeresInvolucradas), 0) FROM Subactividad s")
    long sumMujeresInvolucradas();
}
