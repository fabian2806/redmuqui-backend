package com.redmuqui.platform.macroregion.repository;

import com.redmuqui.platform.macroregion.entity.Macroregion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MacroregionRepository extends JpaRepository<Macroregion, Long> {
    Optional<Macroregion> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
