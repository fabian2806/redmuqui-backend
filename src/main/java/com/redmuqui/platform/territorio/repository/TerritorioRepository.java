package com.redmuqui.platform.territorio.repository;

import com.redmuqui.platform.territorio.entity.Territorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerritorioRepository extends JpaRepository<Territorio, Long> {
    List<Territorio> findByNombreContainingIgnoreCase(String nombre);
}
