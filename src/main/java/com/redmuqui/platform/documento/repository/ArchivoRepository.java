package com.redmuqui.platform.documento.repository;

import com.redmuqui.platform.documento.entity.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, Long> {
    List<Archivo> findByDocumentoId(Long idDocumento);
}
