package com.redmuqui.platform.proyecto.repository;

import com.redmuqui.platform.proyecto.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {
    Optional<Proyecto> findByCodigoInternoIgnoreCase(String codigoInterno);
    boolean existsByCodigoInternoIgnoreCase(String codigoInterno);
    boolean existsByCodigoInternoIgnoreCaseAndIdNot(String codigoInterno, Long id);
}
