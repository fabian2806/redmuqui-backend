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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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
            // RF-052: validar que TODOS los territorios solicitados existan antes de asociar.
            List<Territorio> encontrados = territorioRepository.findAllById(dto.idTerritorios());
            if (encontrados.size() != dto.idTerritorios().size()) {
                throw new ResourceNotFoundException("Territorio", dto.idTerritorios());
            }
            documento.setTerritorios(new HashSet<>(encontrados));
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

    @Transactional
    public DocumentoResponseDTO actualizar(Long id, DocumentoCreateDTO dto) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Documento no encontrado"
                ));

        documento.setTitulo(dto.titulo().trim());
        documento.setDescripcion(dto.descripcion());
        documento.setTipo(dto.tipo().trim());

        if (dto.estado() != null) {
            documento.setEstado(dto.estado());
        }

        documento.setTipoArchivo(dto.tipoArchivo());
        documento.setEnlace(dto.enlace());

        if (dto.idProyecto() != null) {
            Proyecto proyecto = proyectoRepository.findById(dto.idProyecto())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Proyecto no encontrado"
                    ));
            documento.setProyecto(proyecto);
        } else {
            documento.setProyecto(null);
        }

        if (dto.idEjeTematico() != null) {
            EjeTematico ejeTematico = ejeTematicoRepository.findById(dto.idEjeTematico())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Eje temático no encontrado"
                    ));
            documento.setEjeTematico(ejeTematico);
        } else {
            documento.setEjeTematico(null);
        }

        Usuario respElaboracion = usuarioRepository.findById(dto.idRespElaboracion())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Responsable de elaboración no encontrado"
                ));

        documento.setRespElaboracion(respElaboracion);

        if (dto.idRespValidacion() != null) {
            Usuario respValidacion = usuarioRepository.findById(dto.idRespValidacion())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Responsable de validación no encontrado"
                    ));

            documento.setRespValidacion(respValidacion);
        } else {
            documento.setRespValidacion(null);
        }

        if (dto.idTerritorios() != null) {
            Set<Territorio> territorios = new HashSet<>(
                    territorioRepository.findAllById(dto.idTerritorios())
            );

            if (territorios.size() != dto.idTerritorios().size()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Uno o más territorios no existen"
                );
            }

            documento.setTerritorios(territorios);
        } else {
            documento.setTerritorios(new HashSet<>());
        }

        documentoRepository.save(documento);
        return obtener(id);
    }
}
