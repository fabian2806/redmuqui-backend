package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.EnlaceDocumentoDTO;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EnlaceDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.documento.repository.EnlaceDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnlaceDocumentoService {

    private final EnlaceDocumentoRepository enlaceRepository;
    private final DocumentoRepository documentoRepository;

    @Transactional(readOnly = true)
    public List<EnlaceDocumentoDTO> listarPorDocumento(Long idDocumento) {
        asegurarDocumentoExiste(idDocumento);
        return enlaceRepository.findByDocumentoIdOrderByFechaCreacionDesc(idDocumento).stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public EnlaceDocumentoDTO registrar(Long idDocumento, EnlaceDocumentoDTO dto) {
        Documento documento = asegurarDocumentoExiste(idDocumento);
        String url = validarUrl(dto.url());
        String descripcion = dto.descripcion() != null ? dto.descripcion().trim() : "";
        if (descripcion.isBlank()) {
            throw new BusinessException("La descripcion del enlace es obligatoria");
        }

        EnlaceDocumento enlace = EnlaceDocumento.builder()
            .url(url)
            .descripcion(descripcion)
            .documento(documento)
            .build();
        return toDTO(enlaceRepository.save(enlace));
    }

    @Transactional
    public void eliminar(Long idDocumento, Long idEnlace) {
        asegurarDocumentoExiste(idDocumento);
        EnlaceDocumento enlace = buscarEnlaceDelDocumento(idDocumento, idEnlace);
        enlaceRepository.delete(enlace);
    }

    private Documento asegurarDocumentoExiste(Long idDocumento) {
        return documentoRepository.findById(idDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Documento", idDocumento));
    }

    private EnlaceDocumento buscarEnlaceDelDocumento(Long idDocumento, Long idEnlace) {
        EnlaceDocumento enlace = enlaceRepository.findById(idEnlace)
            .orElseThrow(() -> new ResourceNotFoundException("Enlace", idEnlace));
        Long documentoId = enlace.getDocumento() != null ? enlace.getDocumento().getId() : null;
        if (!idDocumento.equals(documentoId)) {
            throw new ResourceNotFoundException("Enlace", idEnlace);
        }
        return enlace;
    }

    private String validarUrl(String value) {
        String url = value != null ? value.trim() : "";
        if (url.isBlank()) {
            throw new BusinessException("La URL del enlace es obligatoria");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                || uri.getHost() == null) {
                throw new BusinessException("La URL del enlace no es valida");
            }
            return url;
        } catch (URISyntaxException ex) {
            throw new BusinessException("La URL del enlace no es valida", ex);
        }
    }

    private EnlaceDocumentoDTO toDTO(EnlaceDocumento e) {
        return new EnlaceDocumentoDTO(
            e.getId(),
            e.getUrl(),
            e.getDescripcion(),
            e.getFechaCreacion(),
            e.getDocumento() != null ? e.getDocumento().getId() : null
        );
    }
}
