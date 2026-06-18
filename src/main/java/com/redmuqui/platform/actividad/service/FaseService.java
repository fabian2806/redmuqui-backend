package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.FaseCreateDTO;
import com.redmuqui.platform.actividad.dto.FaseResponseDTO;
import com.redmuqui.platform.actividad.entity.*;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.FaseRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaseService {

    private final FaseRepository faseRepository;
    private final ProyectoRepository proyectoRepository;
    private final ActividadRepository actividadRepository;
    private final ValidacionCronogramaService validacionCronogramaService;
    private final CronogramaService cronogramaService;
    private final AvanceProyectoService avanceProyectoService;

    @Transactional(readOnly = true)
    public List<FaseResponseDTO> listarPorProyecto(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new ResourceNotFoundException("Proyecto", proyectoId);
        }
        return faseRepository.findByProyectoIdOrderByFechaInicioPlanificadaAscIdAsc(proyectoId)
            .stream().map(this::toDTO).toList();
    }

    @Transactional
    public FaseResponseDTO crear(Long proyectoId, FaseCreateDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", proyectoId));
        validacionCronogramaService.validarFaseEnProyecto(
            proyecto, dto.fechaInicioPlanificada(), dto.fechaFinPlanificada()
        );
        validacionCronogramaService.validarFechaReal(
            dto.fechaFinReal(), dto.fechaInicioReal(), "la fase"
        );
        Fase fase = Fase.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaInicioPlanificada(dto.fechaInicioPlanificada())
            .fechaFinPlanificada(dto.fechaFinPlanificada())
            .fechaInicioReal(dto.fechaInicioReal())
            .fechaFinReal(dto.fechaFinReal())
            .proyecto(proyecto)
            .build();
        return toDTO(faseRepository.save(fase));
    }

    @Transactional
    public FaseResponseDTO actualizar(Long proyectoId, Long id, FaseCreateDTO dto) {
        Fase fase = buscar(proyectoId, id);
        validacionCronogramaService.validarCambioRangoFase(
            fase, dto.fechaInicioPlanificada(), dto.fechaFinPlanificada()
        );
        validacionCronogramaService.validarFechaReal(
            dto.fechaFinReal(), dto.fechaInicioReal(), "la fase"
        );
        cronogramaService.registrarSiCambio(
            TipoEntidadCronograma.FASE,
            fase.getId(),
            fase.getFechaInicioPlanificada(),
            fase.getFechaFinPlanificada(),
            dto.fechaInicioPlanificada(),
            dto.fechaFinPlanificada(),
            dto.motivoReprogramacion()
        );
        fase.setNombre(dto.nombre());
        fase.setDescripcion(dto.descripcion());
        fase.setFechaInicioPlanificada(dto.fechaInicioPlanificada());
        fase.setFechaFinPlanificada(dto.fechaFinPlanificada());
        fase.setFechaInicioReal(dto.fechaInicioReal());
        fase.setFechaFinReal(dto.fechaFinReal());
        avanceProyectoService.recalcularProyecto(proyectoId);
        return toDTO(fase);
    }

    @Transactional
    public void eliminar(Long proyectoId, Long id) {
        Fase fase = buscar(proyectoId, id);
        if (actividadRepository.existsByFaseId(id)) {
            throw new BusinessException("No se puede eliminar una fase que contiene actividades");
        }
        faseRepository.delete(fase);
    }

    private Fase buscar(Long proyectoId, Long id) {
        return faseRepository.findByIdAndProyectoId(id, proyectoId)
            .orElseThrow(() -> new ResourceNotFoundException("Fase", id));
    }

    private FaseResponseDTO toDTO(Fase fase) {
        List<com.redmuqui.platform.actividad.entity.Actividad> actividades =
            actividadRepository.findByFaseIdOrderByFechaInicioPlanificadaAscIdAsc(fase.getId());
        int finalizadas = (int) actividades.stream()
            .filter(a -> a.getEstado() == EstadoActividad.FINALIZADA).count();
        var desfase = cronogramaService.calcular(
            fase.getFechaFinPlanificada(),
            fase.getFechaFinReal(),
            fase.getEstado() == EstadoFase.FINALIZADA
        );
        return new FaseResponseDTO(
            fase.getId(),
            fase.getNombre(),
            fase.getDescripcion(),
            fase.getFechaInicioPlanificada(),
            fase.getFechaFinPlanificada(),
            fase.getFechaInicioReal(),
            fase.getFechaFinReal(),
            fase.getEstado(),
            fase.getPorcentajeAvance(),
            desfase.dias(),
            desfase.estado(),
            fase.getProyecto().getId(),
            actividades.size(),
            finalizadas,
            cronogramaService.listar(TipoEntidadCronograma.FASE, fase.getId())
        );
    }
}
