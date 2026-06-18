package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import com.redmuqui.platform.documento.dto.DocumentoVersionDTO;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.DocumentoVersion;
import com.redmuqui.platform.documento.repository.DocumentoVersionRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoVersionService {

    private final DocumentoVersionRepository repository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public void registrar(Documento documento, String motivo) {
        Usuario usuario = authenticatedUserService.obtenerUsuario();
        int numero = documento.getVersion() == null ? 1 : documento.getVersion().intValue();
        repository.save(DocumentoVersion.builder()
            .documento(documento)
            .numeroVersion(numero)
            .titulo(documento.getTitulo())
            .descripcion(documento.getDescripcion())
            .tipo(documento.getTipo())
            .estado(documento.getEstado())
            .proyecto(documento.getProyecto())
            .ejeTematico(documento.getEjeTematico())
            .responsableElaboracion(documento.getRespElaboracion())
            .responsableValidacion(documento.getRespValidacion())
            .usuarioCambio(usuario)
            .motivoCambio(motivo)
            .build());
    }

    @Transactional(readOnly = true)
    public List<DocumentoVersionDTO> listar(Long documentoId) {
        return repository.findByDocumentoIdOrderByNumeroVersionDesc(documentoId)
            .stream()
            .map(this::toDTO)
            .toList();
    }

    private DocumentoVersionDTO toDTO(DocumentoVersion version) {
        Usuario usuario = version.getUsuarioCambio();
        return new DocumentoVersionDTO(
            version.getId(),
            version.getNumeroVersion(),
            version.getTitulo(),
            version.getDescripcion(),
            version.getTipo(),
            version.getEstado(),
            version.getMotivoCambio(),
            usuario.getId(),
            nombreUsuario(usuario),
            version.getFechaCreacion()
        );
    }

    private String nombreUsuario(Usuario usuario) {
        String nombre = (usuario.getNombres() + " " + usuario.getApellidos()).trim();
        return nombre.isBlank() ? usuario.getEmail() : nombre;
    }
}
