package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.DocumentoCreateDTO;
import com.redmuqui.platform.documento.dto.DocumentoResponseDTO;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ProyectoRepository proyectoRepository;
    private final EjeTematicoRepository ejeTematicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerritorioRepository territorioRepository;

    @Transactional(readOnly = true)
    public Page<DocumentoResponseDTO> listar(Pageable pageable) {
        return documentoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public DocumentoResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Transactional
    public DocumentoResponseDTO crear(DocumentoCreateDTO dto) {
        Documento documento = Documento.builder()
            .titulo(dto.titulo())
            .descripcion(dto.descripcion())
            .tipo(dto.tipo())
            .estado(dto.estado() != null ? dto.estado() : EstadoDocumento.BORRADOR)
            .tipoArchivo(dto.tipoArchivo())
            .enlace(dto.enlace())
            .fechaCarga(LocalDate.now())
            .version(1.0)
            .respElaboracion(usuarioRepository.findById(dto.idRespElaboracion())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idRespElaboracion())))
            .build();

        if (dto.idProyecto() != null) {
            documento.setProyecto(proyectoRepository.findById(dto.idProyecto())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto())));
        }
        if (dto.idEjeTematico() != null) {
            documento.setEjeTematico(ejeTematicoRepository.findById(dto.idEjeTematico())
                .orElseThrow(() -> new ResourceNotFoundException("EjeTematico", dto.idEjeTematico())));
        }
        if (dto.idRespValidacion() != null) {
            documento.setRespValidacion(usuarioRepository.findById(dto.idRespValidacion())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idRespValidacion())));
        }
        if (dto.idTerritorios() != null && !dto.idTerritorios().isEmpty()) {
            documento.setTerritorios(new HashSet<>(territorioRepository.findAllById(dto.idTerritorios())));
        }

        return toDTO(documentoRepository.save(documento));
    }

    @Transactional
    public DocumentoResponseDTO cambiarEstado(Long id, EstadoDocumento nuevoEstado) {
        Documento d = buscarOFallar(id);
        d.setEstado(nuevoEstado);
        return toDTO(d);
    }

    private Documento buscarOFallar(Long id) {
        return documentoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
    }

    private DocumentoResponseDTO toDTO(Documento d) {
        return new DocumentoResponseDTO(
            d.getId(), d.getTitulo(), d.getDescripcion(), d.getTipo(),
            d.getEstado(), d.getTipoArchivo(), d.getFechaCarga(), d.getEnlace(), d.getVersion(),
            d.getProyecto() != null ? d.getProyecto().getId() : null,
            d.getEjeTematico() != null ? d.getEjeTematico().getId() : null,
            d.getRespElaboracion() != null ? d.getRespElaboracion().getId() : null,
            d.getRespValidacion() != null ? d.getRespValidacion().getId() : null,
            d.getTerritorios().stream().map(t -> t.getId()).collect(Collectors.toSet())
        );
    }
}
