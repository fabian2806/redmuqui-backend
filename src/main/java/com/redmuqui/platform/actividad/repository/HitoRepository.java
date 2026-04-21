package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Hito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HitoRepository extends JpaRepository<Hito, Long> {
    List<Hito> findByProyectoId(Long idProyecto);
}
