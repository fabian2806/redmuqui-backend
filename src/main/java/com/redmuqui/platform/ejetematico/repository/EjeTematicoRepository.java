package com.redmuqui.platform.ejetematico.repository;

import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EjeTematicoRepository extends JpaRepository<EjeTematico, Long> {
    Optional<EjeTematico> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
