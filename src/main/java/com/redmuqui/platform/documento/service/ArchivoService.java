package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.ArchivoDTO;
import com.redmuqui.platform.documento.entity.Archivo;
import com.redmuqui.platform.documento.repository.ArchivoRepository;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchivoService {

    private final ArchivoRepository archivoRepository;
    private final DocumentoRepository documentoRepository;

    @Transactional(readOnly = true)
    public List<ArchivoDTO> listarPorDocumento(Long idDocumento) {
        return archivoRepository.findByDocumentoId(idDocumento).stream().map(this::toDTO).toList();
    }

    @Transactional
    public ArchivoDTO crear(Long idDocumento, ArchivoDTO dto) {
        Archivo archivo = Archivo.builder()
            .nombre(dto.nombre())
            .url(dto.url())
            .extension(dto.extension())
            .documento(documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", idDocumento)))
            .build();
        return toDTO(archivoRepository.save(archivo));
    }

    private ArchivoDTO toDTO(Archivo a) {
        return new ArchivoDTO(a.getId(), a.getNombre(), a.getUrl(), a.getExtension(),
            a.getDocumento() != null ? a.getDocumento().getId() : null);
    }
}
