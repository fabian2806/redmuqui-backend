package com.redmuqui.platform.estado.repository;

import com.redmuqui.platform.estado.entity.Estado;
import com.redmuqui.platform.estado.entity.ModuloEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

    boolean existsByCodigoIgnoreCaseAndModulo(String codigo, ModuloEstado modulo);

    boolean existsByNombreIgnoreCaseAndModulo(String nombre, ModuloEstado modulo);

    List<Estado> findByModuloAndActivoTrueOrderByNombreAsc(ModuloEstado modulo);

    Page<Estado> findByModuloOrderByNombreAsc(ModuloEstado modulo, Pageable pageable);
}
