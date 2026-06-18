package com.redmuqui.platform.documento.repository;

import com.redmuqui.platform.documento.entity.DocumentoVersion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoVersionRepository extends JpaRepository<DocumentoVersion, Long> {
    @EntityGraph(attributePaths = "usuarioCambio")
    List<DocumentoVersion> findByDocumentoIdOrderByNumeroVersionDesc(Long documentoId);
}
