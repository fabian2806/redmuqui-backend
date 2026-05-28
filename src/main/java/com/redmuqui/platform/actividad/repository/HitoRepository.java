package com.redmuqui.platform.actividad.repository;

import com.redmuqui.platform.actividad.entity.Hito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HitoRepository extends JpaRepository<Hito, Long> {
    List<Hito> findByProyectoIdOrderByFechaClaveAscIdAsc(Long idProyecto);

    Optional<Hito> findByIdAndProyectoId(Long id, Long idProyecto);
}
