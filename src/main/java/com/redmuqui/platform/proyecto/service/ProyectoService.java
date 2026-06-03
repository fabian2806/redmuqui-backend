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
import com.redmuqui.platform.proyecto.dto.ProyectoTerritorioRequestDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.entity.ProyectoEquipo;
import com.redmuqui.platform.proyecto.entity.ProyectoInstitucion;
import com.redmuqui.platform.proyecto.mapper.ProyectoMapper;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.proyecto.specification.ProyectoSpecification;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private static final Set<String> ROLES_PROYECTO_PERMITIDOS = Set.of(
        "Equipo Técnico",
        "Coordinador",
        "Asesor",
        "Observador"
    );

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
        Long idInstitucion,
        Integer anio,
        Pageable pageable
    ) {
        Specification<Proyecto> filtros = ProyectoSpecification.construir(
            q, estado, idMacroregion, idEjeTematico, idInstitucion, anio
        );
        return proyectoRepository.findAll(filtros, pageable).map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProyectoResponseDTO obtener(Long id) {
        return mapper.toResponseDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public String obtenerUltimoCodigo() {
        return proyectoRepository.findTopByOrderByIdDesc()
            .map(Proyecto::getCodigoInterno)
            .orElse(null);
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
            .estado(dto.estado() != null ? dto.estado() : EstadoProyecto.ACTIVO)
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

        if (proyectoRepository.existsByCodigoInternoIgnoreCaseAndIdNot(dto.codigoInterno(), id)) {
            throw new DuplicateResourceException("Ya existe un proyecto con código: " + dto.codigoInterno());
        }

        validarRangoFechas(dto.fechaInicio(), dto.fechaFinEstimada());
        validarPorcentajeAvance(dto.porcentajeAvance());

        proyecto.setNombre(dto.nombre());
        proyecto.setCodigoInterno(dto.codigoInterno());
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
        validarRolProyecto(dto.rolEnProyecto());
        var usuario = usuarioRepository.findById(dto.idUsuario())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idUsuario()));

        boolean yaExiste = proyecto.getEquipo().stream()
            .anyMatch(pe -> pe.getUsuario().getId().equals(dto.idUsuario()));
        if (yaExiste) {
            throw new BusinessException("El usuario ya es miembro del proyecto");
        }

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
    public void eliminarMiembro(Long idProyecto, Long idUsuario) {
        Proyecto proyecto = buscarOFallar(idProyecto);
        boolean eliminado = proyecto.getEquipo().removeIf(pe -> pe.getUsuario().getId().equals(idUsuario));
        if (!eliminado) {
            throw new ResourceNotFoundException("El usuario con id " + idUsuario + " no es miembro del proyecto");
        }
    }

    @Transactional(readOnly = true)
    public Set<InstitucionParticipacionDTO> obtenerInstituciones(Long idProyecto) {
        Proyecto proyecto = buscarOFallar(idProyecto);
        return proyecto.getInstituciones().stream()
            .map(pi -> {
                InstitucionParticipacionDTO dto = new InstitucionParticipacionDTO();
                dto.setIdInstitucion(pi.getInstitucion().getId());
                dto.setTipoParticipacion(pi.getTipoParticipacion());
                return dto;
            })
            .collect(Collectors.toSet());
    }

    @Transactional
    public void asociarInstituciones(Long idProyecto, AsociarInstitucionesDTO dto) {
        Proyecto proyecto = buscarOFallar(idProyecto);

        Map<Long, InstitucionParticipacionDTO> solicitudesPorInstitucion = dto.getInstituciones().stream()
            .collect(Collectors.toMap(
                InstitucionParticipacionDTO::getIdInstitucion,
                Function.identity(),
                (primera, duplicada) -> duplicada,
                LinkedHashMap::new
            ));

        Map<Long, Institucion> instituciones = cargarInstitucionesOFallar(solicitudesPorInstitucion.keySet());

        proyecto.getInstituciones().removeIf(asociacion ->
            !solicitudesPorInstitucion.containsKey(asociacion.getInstitucion().getId())
        );

        Map<Long, ProyectoInstitucion> asociacionesActuales = proyecto.getInstituciones().stream()
            .collect(Collectors.toMap(pi -> pi.getInstitucion().getId(), Function.identity()));

        for (InstitucionParticipacionDTO item : solicitudesPorInstitucion.values()) {
            ProyectoInstitucion asociacion = asociacionesActuales.get(item.getIdInstitucion());
            if (asociacion == null) {
                asociacion = new ProyectoInstitucion();
                asociacion.setProyecto(proyecto);
                asociacion.setInstitucion(instituciones.get(item.getIdInstitucion()));
                proyecto.getInstituciones().add(asociacion);
            }
            asociacion.setTipoParticipacion(item.getTipoParticipacion());
        }
    }

    @Transactional
    public void actualizarRolMiembro(Long idProyecto, Long idUsuario, String nuevoRol) {
        validarRolProyecto(nuevoRol);
        Proyecto proyecto = buscarOFallar(idProyecto);
        ProyectoEquipo miembro = proyecto.getEquipo().stream()
            .filter(pe -> pe.getUsuario().getId().equals(idUsuario))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("El usuario con id " + idUsuario + " no es miembro del proyecto"));
        miembro.setRolEnProyecto(nuevoRol);
    }

    @Transactional
    public ProyectoResponseDTO cambiarEstado(Long id, EstadoProyecto estado) {
        Proyecto proyecto = buscarOFallar(id);
        proyecto.setEstado(estado);
        return mapper.toResponseDTO(proyecto);
    }

    @Transactional
    public ProyectoResponseDTO actualizarAvance(Long id, Double porcentajeAvance) {
        Proyecto proyecto = buscarOFallar(id);
        validarPorcentajeAvance(porcentajeAvance);
        proyecto.setPorcentajeAvance(porcentajeAvance);
        return mapper.toResponseDTO(proyecto);
    }

    @Transactional
    public void asociarTerritorios(Long idProyecto, ProyectoTerritorioRequestDTO dto) {
        Proyecto proyecto = buscarOFallar(idProyecto);
        Set<Territorio> territorios = cargarTerritoriosOFallar(dto.getTerritoriosIds());
        proyecto.setTerritorios(territorios);
    }

    private Proyecto buscarOFallar(Long id) {
        return proyectoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", id));
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

    private void validarRolProyecto(String rolEnProyecto) {
        if (rolEnProyecto == null || rolEnProyecto.isBlank()) {
            throw new BusinessException("El rol en el proyecto es obligatorio");
        }
        if (!ROLES_PROYECTO_PERMITIDOS.contains(rolEnProyecto)) {
            throw new BusinessException("El rol en el proyecto no es válido");
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
