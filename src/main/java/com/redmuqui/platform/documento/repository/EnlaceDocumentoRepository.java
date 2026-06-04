package com.redmuqui.platform.documento.repository;

import com.redmuqui.platform.documento.entity.EnlaceDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnlaceDocumentoRepository extends JpaRepository<EnlaceDocumento, Long> {
    List<EnlaceDocumento> findByDocumentoIdOrderByFechaCreacionDesc(Long idDocumento);
}
