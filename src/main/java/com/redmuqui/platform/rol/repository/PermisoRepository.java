package com.redmuqui.platform.rol.repository;

import com.redmuqui.platform.rol.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    Optional<Permiso> findByNombreIgnoreCase(String nombre);
}
