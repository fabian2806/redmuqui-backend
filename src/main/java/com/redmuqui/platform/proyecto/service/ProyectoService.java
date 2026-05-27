package com.redmuqui.platform.proyecto.service;

import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.macroregion.entity.Macroregion;
import com.redmuqui.platform.macroregion.repository.MacroregionRepository;
import com.redmuqui.platform.institucion.entity.Institucion;
import com.redmuqui.platform.institucion.repository.InstitucionRepository;
import com.redmuqui.platform.proyecto.dto.AsociarInstitucionesDTO;
import com.redmuqui.platform.proyecto.dto.EquipoMemberDTO;
import com.redmuqui.platform.proyecto.dto.InstitucionParticipacionDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoCreateDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.entity.ProyectoEquipo;
import com.redmuqui.platform.proyecto.entity.ProyectoInstitucion;
import com.redmuqui.platform.proyecto.mapper.ProyectoMapper;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final MacroregionRepository macroregionRepository;
    private final EjeTematicoRepository ejeTematicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerritorioRepository territorioRepository;
    private final InstitucionRepository institucionRepository;
    private final ProyectoMapper mapper;

    @Transactional(readOnly = true)
    public Page<ProyectoResponseDTO> listar(
        String q,
        EstadoProyecto estado,
        Long idMacroregion,
        Long idEjeTematico,
        Pageable pageable
    ) {
        Specification<Proyecto> filtros = construirFiltros(q, estado, idMacroregion, idEjeTematico);
        return proyectoRepository.findAll(filtros, pageable).map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProyectoResponseDTO obtener(Long id) {
        return mapper.toResponseDTO(buscarOFallar(id));
    }

    @Transactional
    public ProyectoResponseDTO crear(ProyectoCreateDTO dto) {
        if (proyectoRepository.existsByCodigoInternoIgnoreCase(dto.codigoInterno())) {
            throw new DuplicateResourceException("Ya existe un proyecto con código: " + dto.codigoInterno());
        }

        validarRangoFechas(dto.fechaInicio(), dto.fechaFinEstimada());

        Proyecto proyecto = Proyecto.builder()
            .nombre(dto.nombre())
            .codigoInterno(dto.codigoInterno())
            .descripcion(dto.descripcion())
            .objetivoGeneral(dto.objetivoGeneral())
            .fechaInicio(dto.fechaInicio())
            .fechaFinEstimada(dto.fechaFinEstimada())
            .estado(dto.estado() != null ? dto.estado() : EstadoProyecto.PENDIENTE)
            .nivelPrioridad(dto.nivelPrioridad())
            .presupuesto(dto.presupuesto())
            .porcentajeAvance(0.0)
            .build();

        Set<Long> idsMacroregiones = resolverIdsMacroregiones(dto.idMacroregion(), dto.idMacroregiones());
        if (!idsMacroregiones.isEmpty()) {
            proyecto.setMacroregiones(cargarMacroregionesOFallar(idsMacroregiones));
        }
        if (dto.idEjeTematico() != null) {
            proyecto.setEjeTematico(ejeTematicoRepository.findById(dto.idEjeTematico())
                .orElseThrow(() -> new ResourceNotFoundException("EjeTematico", dto.idEjeTematico())));
        }
        if (dto.idResponsablePrincipal() != null) {
            proyecto.setResponsablePrincipal(usuarioRepository.findById(dto.idResponsablePrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsablePrincipal())));
        }
        if (dto.idTerritorios() != null && !dto.idTerritorios().isEmpty()) {
            proyecto.setTerritorios(cargarTerritoriosOFallar(dto.idTerritorios()));
        }

        return mapper.toResponseDTO(proyectoRepository.save(proyecto));
    }

    @Transactional
    public ProyectoResponseDTO actualizar(Long id, ProyectoUpdateDTO dto) {
        Proyecto proyecto = buscarOFallar(id);

        validarRangoFechas(dto.fechaInicio(), dto.fechaFinEstimada());
        validarPorcentajeAvance(dto.porcentajeAvance());

        proyecto.setNombre(dto.nombre());
        proyecto.setDescripcion(dto.descripcion());
        proyecto.setObjetivoGeneral(dto.objetivoGeneral());
        proyecto.setFechaInicio(dto.fechaInicio());
        proyecto.setFechaFinEstimada(dto.fechaFinEstimada());
        if (dto.estado() != null) proyecto.setEstado(dto.estado());
        proyecto.setNivelPrioridad(dto.nivelPrioridad());
        if (dto.porcentajeAvance() != null) proyecto.setPorcentajeAvance(dto.porcentajeAvance());
        proyecto.setPresupuesto(dto.presupuesto());

        Set<Long> idsMacroregiones = resolverIdsMacroregiones(dto.idMacroregion(), dto.idMacroregiones());
        proyecto.setMacroregiones(cargarMacroregionesOFallar(idsMacroregiones));
        proyecto.setEjeTematico(dto.idEjeTematico() != null
            ? ejeTematicoRepository.findById(dto.idEjeTematico())
                .orElseThrow(() -> new ResourceNotFoundException("EjeTematico", dto.idEjeTematico()))
            : null);
        proyecto.setResponsablePrincipal(dto.idResponsablePrincipal() != null
            ? usuarioRepository.findById(dto.idResponsablePrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsablePrincipal()))
            : null);
        if (dto.idTerritorios() != null) {
            proyecto.setTerritorios(cargarTerritoriosOFallar(dto.idTerritorios()));
        }

        return mapper.toResponseDTO(proyecto);
    }

    @Transactional
    public void agregarMiembro(Long idProyecto, EquipoMemberDTO dto) {
        Proyecto proyecto = buscarOFallar(idProyecto);
        var usuario = usuarioRepository.findById(dto.idUsuario())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idUsuario()));

        ProyectoEquipo miembro = ProyectoEquipo.builder()
            .proyecto(proyecto)
            .usuario(usuario)
            .rolEnProyecto(dto.rolEnProyecto())
            .build();

        proyecto.getEquipo().add(miembro);
    }

    @Transactional(readOnly = true)
    public Set<EquipoMemberDTO> obtenerEquipo(Long idProyecto) {
        Proyecto proyecto = buscarOFallar(idProyecto);
        return proyecto.getEquipo().stream()
            .map(pe -> new EquipoMemberDTO(pe.getUsuario().getId(), pe.getRolEnProyecto()))
            .collect(Collectors.toSet());
    }

    @Transactional
    public void asociarInstituciones(Long idProyecto, AsociarInstitucionesDTO dto) {
        Proyecto proyecto = buscarOFallar(idProyecto);
        Map<Long, Institucion> instituciones = cargarInstitucionesOFallar(
            dto.getInstituciones().stream()
                .map(InstitucionParticipacionDTO::getIdInstitucion)
                .collect(Collectors.toSet())
        );

        for (InstitucionParticipacionDTO item : dto.getInstituciones()) {
            ProyectoInstitucion asociacion = new ProyectoInstitucion();
            asociacion.setProyecto(proyecto);
            asociacion.setInstitucion(instituciones.get(item.getIdInstitucion()));
            asociacion.setTipoParticipacion(item.getTipoParticipacion());
            proyecto.getInstituciones().add(asociacion);
        }
    }

    private Proyecto buscarOFallar(Long id) {
        return proyectoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", id));
    }

    private Specification<Proyecto> construirFiltros(
        String q,
        EstadoProyecto estado,
        Long idMacroregion,
        Long idEjeTematico
    ) {
        Specification<Proyecto> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("codigoInterno")), patron)
            ));
        }

        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }

        if (idMacroregion != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(root.join("macroregiones", JoinType.LEFT).get("id"), idMacroregion);
            });
        }

        if (idEjeTematico != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ejeTematico").get("id"), idEjeTematico));
        }

        return spec;
    }

    private void validarRangoFechas(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFinEstimada) {
        if (fechaInicio != null && fechaFinEstimada != null && fechaFinEstimada.isBefore(fechaInicio)) {
            throw new BusinessException("La fecha de fin estimada no puede ser anterior a la fecha de inicio");
        }
    }

    private void validarPorcentajeAvance(Double porcentajeAvance) {
        if (porcentajeAvance != null && (porcentajeAvance < 0 || porcentajeAvance > 100)) {
            throw new BusinessException("El porcentaje de avance debe estar entre 0 y 100");
        }
    }

    private Set<Long> resolverIdsMacroregiones(Long idMacroregion, Set<Long> idMacroregiones) {
        Set<Long> ids = new HashSet<>();
        if (idMacroregiones != null) {
            ids.addAll(idMacroregiones);
        }
        if (idMacroregion != null) {
            ids.add(idMacroregion);
        }
        return ids;
    }

    private Set<Macroregion> cargarMacroregionesOFallar(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Macroregion> macroregiones = macroregionRepository.findAllById(ids);
        Set<Long> encontrados = macroregiones.stream()
            .map(Macroregion::getId)
            .collect(Collectors.toSet());
        Set<Long> faltantes = ids.stream()
            .filter(id -> !encontrados.contains(id))
            .collect(Collectors.toSet());

        if (!faltantes.isEmpty()) {
            throw new ResourceNotFoundException("Macroregion", faltantes.toString());
        }

        return new HashSet<>(macroregiones);
    }

    private Set<Territorio> cargarTerritoriosOFallar(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Territorio> territorios = territorioRepository.findAllById(ids);
        Set<Long> encontrados = territorios.stream()
            .map(Territorio::getId)
            .collect(Collectors.toSet());
        Set<Long> faltantes = ids.stream()
            .filter(id -> !encontrados.contains(id))
            .collect(Collectors.toSet());

        if (!faltantes.isEmpty()) {
            throw new ResourceNotFoundException("Territorio", faltantes.toString());
        }

        return new HashSet<>(territorios);
    }

    private Map<Long, Institucion> cargarInstitucionesOFallar(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        List<Institucion> instituciones = institucionRepository.findAllById(ids);
        Set<Long> encontrados = instituciones.stream()
            .map(Institucion::getId)
            .collect(Collectors.toSet());
        Set<Long> faltantes = ids.stream()
            .filter(id -> !encontrados.contains(id))
            .collect(Collectors.toSet());

        if (!faltantes.isEmpty()) {
            throw new ResourceNotFoundException("Institucion", faltantes.toString());
        }

        return instituciones.stream()
            .collect(Collectors.toMap(Institucion::getId, Function.identity()));
    }
}
