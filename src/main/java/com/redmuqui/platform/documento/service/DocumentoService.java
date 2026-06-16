package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.DocumentoCreateDTO;
import com.redmuqui.platform.documento.dto.DocumentoResponseDTO;
import com.redmuqui.platform.documento.dto.DocumentoUpdateDTO;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ProyectoRepository proyectoRepository;
    private final EjeTematicoRepository ejeTematicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerritorioRepository territorioRepository;

    /**
     * Tipos de documento permitidos al registrar (RF-046). Debe mantenerse
     * idéntico a la constante TIPOS_DOCUMENTO del frontend (lib/types.ts).
     */
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "Informe", "Pronunciamiento", "Investigación", "Manual", "Cartilla", "Resumen técnico"
    );

    @Transactional(readOnly = true)
    public Page<DocumentoResponseDTO> listar(Pageable pageable) {
        return documentoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public DocumentoResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Transactional
    public void eliminar(Long id) {
        Documento documento = buscarOFallar(id);
        documentoRepository.delete(documento);
    }

    @Transactional
    public DocumentoResponseDTO crear(DocumentoCreateDTO dto) {
        // RF-046: el tipo debe ser uno de los valores permitidos.
        String tipo = dto.tipo() != null ? dto.tipo().trim() : null;
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new BusinessException(
                "El tipo de documento no es válido. Valores permitidos: " + TIPOS_PERMITIDOS);
        }

        Documento documento = Documento.builder()
            .titulo(dto.titulo())
            .descripcion(dto.descripcion())
            .tipo(tipo)
            .estado(dto.estado() != null ? dto.estado() : EstadoDocumento.BORRADOR)
            .tipoArchivo(dto.tipoArchivo())
            .enlace(dto.enlace())
            .fechaCarga(java.time.LocalDate.now())
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
            // RF-052: validar que TODOS los territorios solicitados existan antes de asociar.
            List<Territorio> encontrados = territorioRepository.findAllById(dto.idTerritorios());
            if (encontrados.size() != dto.idTerritorios().size()) {
                throw new ResourceNotFoundException("Territorio", dto.idTerritorios());
            }
            documento.setTerritorios(new HashSet<>(encontrados));
        }

        return toDTO(documentoRepository.save(documento));
    }

    /**
     * RF-056: cambia el estado de un documento siguiendo el flujo editorial.
     * Transiciones válidas y authority requerida:
     *   BORRADOR     → EN_REVISION : DOCUMENTOS_UPDATE
     *   EN_REVISION  → PUBLICADO   : DOCUMENTOS_VALIDATE
     *   PUBLICADO    → EN_REVISION : DOCUMENTOS_VALIDATE
     *   EN_REVISION  → BORRADOR    : DOCUMENTOS_VALIDATE
     */
    @Transactional
    public DocumentoResponseDTO cambiarEstado(Long id, EstadoDocumento nuevoEstado) {
        Documento d = buscarOFallar(id);
        EstadoDocumento estadoActual = d.getEstado();

        String authorityRequerida = resolverAuthorityParaTransicion(estadoActual, nuevoEstado);

        if (!tieneAuthority(authorityRequerida)) {
            throw new BusinessException(
                "Se requiere el permiso " + authorityRequerida
                    + " para realizar la transición " + estadoActual + " → " + nuevoEstado + ".");
        }

        d.setEstado(nuevoEstado);
        return toDTO(d);
    }

    private String resolverAuthorityParaTransicion(EstadoDocumento desde, EstadoDocumento hacia) {
        if (desde == EstadoDocumento.BORRADOR && hacia == EstadoDocumento.EN_REVISION) {
            return "DOCUMENTOS_UPDATE";
        }
        if (desde == EstadoDocumento.EN_REVISION && hacia == EstadoDocumento.PUBLICADO) {
            return "DOCUMENTOS_VALIDATE";
        }
        if (desde == EstadoDocumento.PUBLICADO && hacia == EstadoDocumento.EN_REVISION) {
            return "DOCUMENTOS_VALIDATE";
        }
        if (desde == EstadoDocumento.EN_REVISION && hacia == EstadoDocumento.BORRADOR) {
            return "DOCUMENTOS_VALIDATE";
        }
        throw new BusinessException(
            "Transición de estado no permitida: " + desde + " → " + hacia + ".");
    }

    private boolean tieneAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
            .anyMatch(a -> authority.equals(a.getAuthority()));
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

    @Transactional
    public DocumentoResponseDTO actualizar(Long id, DocumentoUpdateDTO dto) {
        Documento documento = buscarOFallar(id);

        String tipo = dto.tipo() != null ? dto.tipo().trim() : null;
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new BusinessException(
                "El tipo de documento no es válido. Valores permitidos: " + TIPOS_PERMITIDOS);
        }

        if (dto.estado() == EstadoDocumento.PUBLICADO
                && documento.getEstado() != EstadoDocumento.PUBLICADO
                && !tieneAuthority("DOCUMENTOS_VALIDATE")) {
            throw new BusinessException(
                "Se requiere el permiso DOCUMENTOS_VALIDATE para publicar un documento.");
        }

        documento.setTitulo(dto.titulo());
        documento.setDescripcion(dto.descripcion());
        documento.setTipo(tipo);
        documento.setEstado(dto.estado());
        documento.setTipoArchivo(dto.tipoArchivo());
        documento.setEnlace(dto.enlace());
        documento.setFechaCarga(dto.fechaCarga());

        if (dto.idProyecto() != null) {
            documento.setProyecto(proyectoRepository.findById(dto.idProyecto())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto())));
        } else {
            documento.setProyecto(null);
        }

        if (dto.idEjeTematico() != null) {
            documento.setEjeTematico(ejeTematicoRepository.findById(dto.idEjeTematico())
                .orElseThrow(() -> new ResourceNotFoundException("EjeTematico", dto.idEjeTematico())));
        } else {
            documento.setEjeTematico(null);
        }

        documento.setRespElaboracion(usuarioRepository.findById(dto.idRespElaboracion())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idRespElaboracion())));

        if (dto.idRespValidacion() != null) {
            documento.setRespValidacion(usuarioRepository.findById(dto.idRespValidacion())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idRespValidacion())));
        } else {
            documento.setRespValidacion(null);
        }

        if (dto.idTerritorios() != null && !dto.idTerritorios().isEmpty()) {
            List<Territorio> encontrados = territorioRepository.findAllById(dto.idTerritorios());
            if (encontrados.size() != dto.idTerritorios().size()) {
                throw new ResourceNotFoundException("Territorio", dto.idTerritorios());
            }
            documento.setTerritorios(new HashSet<>(encontrados));
        } else {
            documento.setTerritorios(new HashSet<>());
        }

        return toDTO(documentoRepository.save(documento));
    }
}
