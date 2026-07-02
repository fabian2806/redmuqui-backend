package com.redmuqui.platform.tipodocumento.repository;

import com.redmuqui.platform.tipodocumento.entity.TipoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    List<TipoDocumento> findByActivoTrueOrderByNombreAsc();

    Page<TipoDocumento> findAllByOrderByNombreAsc(Pageable pageable);
}
