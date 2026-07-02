package com.redmuqui.platform.moneda.repository;

import com.redmuqui.platform.moneda.entity.Moneda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonedaRepository extends JpaRepository<Moneda, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Moneda> findByActivoTrueOrderByNombreAsc();

    Page<Moneda> findAllByOrderByNombreAsc(Pageable pageable);
}
