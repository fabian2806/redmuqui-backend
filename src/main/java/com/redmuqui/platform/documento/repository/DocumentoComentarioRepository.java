package com.redmuqui.platform.documento.repository;

import com.redmuqui.platform.documento.entity.DocumentoComentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoComentarioRepository extends JpaRepository<DocumentoComentario, Long> {
    @EntityGraph(attributePaths = "usuario")
    Page<DocumentoComentario> findByDocumentoIdOrderByFechaCreacionDesc(Long documentoId, Pageable pageable);
}
