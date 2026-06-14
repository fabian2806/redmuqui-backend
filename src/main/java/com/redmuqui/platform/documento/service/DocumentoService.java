package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.actividad.entity.EstadoSubactividad;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.actividad.service.AvanceProyectoService;
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
import com.redmuqui.platform.documento.entity.TipoVinculoDocumento;
import com.redmuqui.platform.documento.repository.ArchivoRepository;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.documento.specification.DocumentoSpecification;
import com.redmuqui.platform.common.audit.AuthenticatedUserService;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ProyectoRepository proyectoRepository;
    private final EjeTematicoRepository ejeTematicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerritorioRepository territorioRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final DocumentoVersionService documentoVersionService;
    private final SubactividadRepository subactividadRepository;
    private final ArchivoRepository archivoRepository;
    private final AvanceProyectoService avanceProyectoService;

    /**
     * Tipos de documento permitidos al registrar (RF-046). Debe mantenerse
     * idéntico a la constante TIPOS_DOCUMENTO del frontend (lib/types.ts).
     */
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "Informe", "Pronunciamiento", "Investigación", "Manual", "Cartilla", "Resumen técnico"
    );

    @Transactional(readOnly = true)
    public Page<DocumentoResponseDTO> listar(
        String q,
        Long idProyecto,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        EstadoDocumento estado,
        Pageable pageable
    ) {
        return documentoRepository.findAll(
            DocumentoSpecification.construir(q, idProyecto, fechaDesde, fechaHasta, estado),
            pageable
        ).map(this::toDTO);
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
        if (dto.estado() != null && dto.estado() != EstadoDocumento.BORRADOR) {
            throw new BusinessException(
                "Todo documento debe registrarse inicialmente como borrador.");
        }

        VinculoSubactividad vinculo = resolverVinculo(
            dto.idSubactividad(), dto.idProyecto(), dto.tipoVinculo(), null
        );

        Documento documento = Documento.builder()
            .titulo(dto.titulo())
            .descripcion(dto.descripcion())
            .tipo(tipo)
            .estado(dto.estado() != null ? dto.estado() : EstadoDocumento.BORRADOR)
            .tipoArchivo(dto.tipoArchivo())
            .enlace(dto.enlace())
            .fechaCarga(java.time.LocalDate.now())
            .version(1.0)
            .usuarioCarga(authenticatedUserService.obtenerUsuario())
            .respElaboracion(usuarioRepository.findById(dto.idRespElaboracion())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idRespElaboracion())))
            .subactividad(vinculo.subactividad())
            .tipoVinculo(vinculo.tipoVinculo())
            .build();

        if (vinculo.proyecto() != null) {
            documento.setProyecto(vinculo.proyecto());
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

        Documento guardado = documentoRepository.save(documento);
        sincronizarSubactividad(guardado);
        documentoVersionService.registrar(guardado, "Creación del documento");
        return toDTO(guardado);
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

        validarTransicionYPermiso(estadoActual, nuevoEstado);
        validarEnvioRevision(d, estadoActual, nuevoEstado);
        validarPublicacion(d, nuevoEstado);
        d.setEstado(nuevoEstado);
        sincronizarSubactividad(d);
        incrementarVersion(d);
        documentoVersionService.registrar(d, "Cambio de estado: " + estadoActual + " → " + nuevoEstado);
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

    private void validarTransicionYPermiso(EstadoDocumento desde, EstadoDocumento hacia) {
        String authorityRequerida = resolverAuthorityParaTransicion(desde, hacia);
        if (!tieneAuthority(authorityRequerida)) {
            throw new BusinessException(
                "Se requiere el permiso " + authorityRequerida
                    + " para realizar la transición " + desde + " → " + hacia + ".");
        }
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
        Subactividad subactividad = d.getSubactividad();
        return new DocumentoResponseDTO(
            d.getId(), d.getTitulo(), d.getDescripcion(), d.getTipo(),
            d.getEstado(), d.getTipoArchivo(), d.getFechaCarga(), d.getEnlace(), d.getVersion(),
            d.getProyecto() != null ? d.getProyecto().getId() : null,
            subactividad != null ? subactividad.getId() : null,
            subactividad != null ? subactividad.getNombre() : null,
            subactividad != null ? subactividad.getActividad().getId() : null,
            d.getTipoVinculo(),
            d.getEjeTematico() != null ? d.getEjeTematico().getId() : null,
            d.getRespElaboracion() != null ? d.getRespElaboracion().getId() : null,
            d.getRespValidacion() != null ? d.getRespValidacion().getId() : null,
            d.getTerritorios().stream().map(t -> t.getId()).collect(Collectors.toSet()),
            d.getUsuarioCarga() != null ? d.getUsuarioCarga().getId() : null,
            d.getUsuarioCarga() != null ? nombreUsuario(d.getUsuarioCarga()) : null
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

        VinculoSubactividad vinculo = resolverVinculo(
            dto.idSubactividad(), dto.idProyecto(), dto.tipoVinculo(), documento
        );
        EstadoDocumento estadoAnterior = documento.getEstado();
        Long idSubactividadAnterior = documento.getSubactividad() != null
            ? documento.getSubactividad().getId()
            : null;
        if (dto.estado() != estadoAnterior) {
            validarTransicionYPermiso(estadoAnterior, dto.estado());
        }

        documento.setTitulo(dto.titulo());
        documento.setDescripcion(dto.descripcion());
        documento.setTipo(tipo);
        documento.setTipoArchivo(dto.tipoArchivo());
        documento.setEnlace(dto.enlace());
        documento.setFechaCarga(dto.fechaCarga());
        documento.setSubactividad(vinculo.subactividad());
        documento.setTipoVinculo(vinculo.tipoVinculo());

        documento.setProyecto(vinculo.proyecto());

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

        if (dto.estado() != estadoAnterior) {
            validarEnvioRevision(documento, estadoAnterior, dto.estado());
            validarPublicacion(documento, dto.estado());
        }
        documento.setEstado(dto.estado());
        if (dto.estado() != estadoAnterior
                || !Objects.equals(
                    idSubactividadAnterior,
                    documento.getSubactividad() != null ? documento.getSubactividad().getId() : null)) {
            sincronizarSubactividad(documento);
        }
        incrementarVersion(documento);
        Documento guardado = documentoRepository.save(documento);
        documentoVersionService.registrar(guardado, "Actualización de datos del documento");
        return toDTO(guardado);
    }

    private void incrementarVersion(Documento documento) {
        double actual = documento.getVersion() == null ? 0D : documento.getVersion();
        documento.setVersion(Math.floor(actual) + 1D);
    }

    private VinculoSubactividad resolverVinculo(
            Long idSubactividad,
            Long idProyecto,
            TipoVinculoDocumento tipoSolicitado,
            Documento documentoExistente) {
        Subactividad subactividadExistente = documentoExistente != null
            ? documentoExistente.getSubactividad()
            : null;
        Long idSubactividadEfectivo = idSubactividad != null
            ? idSubactividad
            : subactividadExistente != null ? subactividadExistente.getId() : null;
        TipoVinculoDocumento tipoEfectivo = tipoSolicitado != null
            ? tipoSolicitado
            : idSubactividadEfectivo != null
                ? TipoVinculoDocumento.ENTREGABLE_FINAL
                : TipoVinculoDocumento.GENERAL;

        if (subactividadExistente != null
                && idSubactividad != null
                && !Objects.equals(subactividadExistente.getId(), idSubactividad)) {
            throw new BusinessException(
                "No se puede cambiar la subactividad asociada a un entregable existente.");
        }
        if (tipoEfectivo == TipoVinculoDocumento.ENTREGABLE_FINAL
                && idSubactividadEfectivo == null) {
            throw new BusinessException(
                "Un entregable final debe estar asociado a una subactividad.");
        }
        if (tipoEfectivo == TipoVinculoDocumento.GENERAL
                && idSubactividadEfectivo != null) {
            throw new BusinessException(
                "Los documentos asociados a una subactividad deben ser entregables finales.");
        }

        Subactividad subactividad = idSubactividadEfectivo != null
            ? subactividadRepository.findById(idSubactividadEfectivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Subactividad", idSubactividadEfectivo))
            : null;

        if (subactividad != null
                && (documentoExistente == null
                    || !Objects.equals(documentoExistente.getSubactividad() != null
                        ? documentoExistente.getSubactividad().getId()
                        : null, subactividad.getId()))
                && documentoRepository.existsBySubactividadIdAndTipoVinculo(
                    subactividad.getId(), TipoVinculoDocumento.ENTREGABLE_FINAL)) {
            throw new BusinessException(
                "La subactividad ya tiene un documento entregable final asociado.");
        }

        Proyecto proyecto;
        if (subactividad != null) {
            proyecto = subactividad.getActividad().getProyecto();
            if (idProyecto != null && !Objects.equals(proyecto.getId(), idProyecto)) {
                throw new BusinessException(
                    "El proyecto del documento no coincide con el proyecto de la subactividad.");
            }
        } else if (idProyecto != null) {
            proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", idProyecto));
        } else {
            proyecto = null;
        }

        return new VinculoSubactividad(subactividad, proyecto, tipoEfectivo);
    }

    private void validarPublicacion(Documento documento, EstadoDocumento nuevoEstado) {
        if (nuevoEstado != EstadoDocumento.PUBLICADO) return;
        if (documento.getRespValidacion() == null) {
            throw new BusinessException(
                "Debe asignar un responsable de validación antes de publicar el documento.");
        }
        if (documento.getId() == null || !archivoRepository.existsByDocumentoId(documento.getId())) {
            throw new BusinessException(
                "Debe adjuntar al menos un archivo antes de publicar el documento.");
        }
        if (documento.getTipoVinculo() != TipoVinculoDocumento.ENTREGABLE_FINAL) return;

        Subactividad subactividad = documento.getSubactividad();
        if (subactividad == null) {
            throw new BusinessException(
                "El entregable final no tiene una subactividad asociada.");
        }
        if (subactividad.getCostoReal() == null) {
            throw new BusinessException(
                "Debe registrar el costo real de la subactividad antes de publicar su entregable.");
        }
        if (subactividad.getFechaInicioPlanificada() != null
                && LocalDate.now().isBefore(subactividad.getFechaInicioPlanificada())) {
            throw new BusinessException(
                "No se puede publicar el entregable antes de la fecha de inicio planificada de la subactividad.");
        }
    }

    private void validarEnvioRevision(
            Documento documento,
            EstadoDocumento estadoActual,
            EstadoDocumento nuevoEstado) {
        if (estadoActual != EstadoDocumento.BORRADOR
                || nuevoEstado != EstadoDocumento.EN_REVISION) {
            return;
        }
        if (documento.getRespValidacion() == null) {
            throw new BusinessException(
                "Debe asignar un responsable de validación antes de enviar el documento a revisión.");
        }
        if (documento.getId() == null || !archivoRepository.existsByDocumentoId(documento.getId())) {
            throw new BusinessException(
                "Debe adjuntar al menos un archivo antes de enviar el documento a revisión.");
        }
    }

    private void sincronizarSubactividad(Documento documento) {
        if (documento.getTipoVinculo() != TipoVinculoDocumento.ENTREGABLE_FINAL
                || documento.getSubactividad() == null) {
            return;
        }

        Subactividad subactividad = documento.getSubactividad();
        if (documento.getEstado() == EstadoDocumento.PUBLICADO) {
            subactividad.setEstado(EstadoSubactividad.FINALIZADA);
            subactividad.setPorcentajeAvance(100);
            if (subactividad.getFechaInicioReal() == null) {
                subactividad.setFechaInicioReal(subactividad.getFechaInicioPlanificada());
            }
            subactividad.setFechaFinReal(LocalDate.now());
        } else {
            subactividad.setEstado(EstadoSubactividad.EN_CURSO);
            subactividad.setPorcentajeAvance(
                documento.getEstado() == EstadoDocumento.EN_REVISION ? 50 : 25);
            if (subactividad.getFechaInicioReal() == null) {
                subactividad.setFechaInicioReal(subactividad.getFechaInicioPlanificada());
            }
            subactividad.setFechaFinReal(null);
        }

        subactividadRepository.save(subactividad);
        avanceProyectoService.recalcularActividad(subactividad.getActividad().getId());
    }

    private String nombreUsuario(Usuario usuario) {
        String nombre = (usuario.getNombres() + " " + usuario.getApellidos()).trim();
        return nombre.isBlank() ? usuario.getEmail() : nombre;
    }

    private record VinculoSubactividad(
        Subactividad subactividad,
        Proyecto proyecto,
        TipoVinculoDocumento tipoVinculo
    ) {}
}
