package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.DocumentoComentarioDTO;
import com.redmuqui.platform.documento.dto.DocumentoComentarioRequest;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.DocumentoComentario;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoComentarioRepository;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentoComentarioService {

    private final DocumentoComentarioRepository repository;
    private final DocumentoRepository documentoRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public DocumentoComentarioDTO crear(Long documentoId, DocumentoComentarioRequest request) {
        Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));
        if (documento.getEstado() != EstadoDocumento.EN_REVISION) {
            throw new BusinessException("Solo se pueden registrar comentarios cuando el documento está en revisión");
        }
        DocumentoComentario comentario = repository.save(DocumentoComentario.builder()
            .documento(documento)
            .usuario(authenticatedUserService.obtenerUsuario())
            .comentario(request.comentario().trim())
            .build());
        return toDTO(comentario);
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentoComentarioDTO> listar(Long documentoId, Pageable pageable) {
        if (!documentoRepository.existsById(documentoId)) {
            throw new ResourceNotFoundException("Documento", documentoId);
        }
        return PageResponse.from(repository.findByDocumentoIdOrderByFechaCreacionDesc(documentoId, pageable)
            .map(this::toDTO));
    }

    private DocumentoComentarioDTO toDTO(DocumentoComentario comentario) {
        Usuario usuario = comentario.getUsuario();
        String nombre = (usuario.getNombres() + " " + usuario.getApellidos()).trim();
        return new DocumentoComentarioDTO(
            comentario.getId(),
            comentario.getComentario(),
            usuario.getId(),
            nombre.isBlank() ? usuario.getEmail() : nombre,
            comentario.getFechaCreacion()
        );
    }
}
