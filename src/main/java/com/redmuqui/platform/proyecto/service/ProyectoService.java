package com.redmuqui.platform.proyecto.service;

import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.macroregion.repository.MacroregionRepository;
import com.redmuqui.platform.proyecto.dto.EquipoMemberDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoCreateDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.entity.ProyectoEquipo;
import com.redmuqui.platform.proyecto.mapper.ProyectoMapper;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final MacroregionRepository macroregionRepository;
    private final EjeTematicoRepository ejeTematicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerritorioRepository territorioRepository;
    private final ProyectoMapper mapper;

    @Transactional(readOnly = true)
    public Page<ProyectoResponseDTO> listar(Pageable pageable) {
        return proyectoRepository.findAll(pageable).map(mapper::toResponseDTO);
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

        if (dto.idMacroregion() != null) {
            proyecto.setMacroregion(macroregionRepository.findById(dto.idMacroregion())
                .orElseThrow(() -> new ResourceNotFoundException("Macroregion", dto.idMacroregion())));
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
            proyecto.setTerritorios(new HashSet<>(territorioRepository.findAllById(dto.idTerritorios())));
        }

        return mapper.toResponseDTO(proyectoRepository.save(proyecto));
    }

    @Transactional
    public ProyectoResponseDTO actualizar(Long id, ProyectoUpdateDTO dto) {
        Proyecto proyecto = buscarOFallar(id);

        proyecto.setNombre(dto.nombre());
        proyecto.setDescripcion(dto.descripcion());
        proyecto.setObjetivoGeneral(dto.objetivoGeneral());
        proyecto.setFechaInicio(dto.fechaInicio());
        proyecto.setFechaFinEstimada(dto.fechaFinEstimada());
        if (dto.estado() != null) proyecto.setEstado(dto.estado());
        proyecto.setNivelPrioridad(dto.nivelPrioridad());
        if (dto.porcentajeAvance() != null) proyecto.setPorcentajeAvance(dto.porcentajeAvance());
        proyecto.setPresupuesto(dto.presupuesto());

        proyecto.setMacroregion(dto.idMacroregion() != null
            ? macroregionRepository.findById(dto.idMacroregion())
                .orElseThrow(() -> new ResourceNotFoundException("Macroregion", dto.idMacroregion()))
            : null);
        proyecto.setEjeTematico(dto.idEjeTematico() != null
            ? ejeTematicoRepository.findById(dto.idEjeTematico())
                .orElseThrow(() -> new ResourceNotFoundException("EjeTematico", dto.idEjeTematico()))
            : null);
        proyecto.setResponsablePrincipal(dto.idResponsablePrincipal() != null
            ? usuarioRepository.findById(dto.idResponsablePrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsablePrincipal()))
            : null);
        if (dto.idTerritorios() != null) {
            proyecto.setTerritorios(new HashSet<>(territorioRepository.findAllById(dto.idTerritorios())));
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

    private Proyecto buscarOFallar(Long id) {
        return proyectoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", id));
    }
}
