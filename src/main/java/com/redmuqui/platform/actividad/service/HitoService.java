package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.HitoCreateDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.actividad.entity.*;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.FaseRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HitoService {

    private final HitoRepository hitoRepository;
    private final ProyectoRepository proyectoRepository;
    private final FaseRepository faseRepository;
    private final ActividadRepository actividadRepository;
    private final AvanceProyectoService avanceProyectoService;
    private final CronogramaService cronogramaService;

    @Transactional(readOnly = true)
    public List<HitoResponseDTO> listarPorProyecto(Long idProyecto) {
        validarProyectoExiste(idProyecto);
        return hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(idProyecto)
            .stream().map(this::toDTO).toList();
    }

    @Transactional
    public HitoResponseDTO crear(HitoCreateDTO dto) {
        if (dto.idProyecto() == null) {
            throw new BusinessException("El proyecto es obligatorio para registrar un hito");
        }
        return crear(dto.idProyecto(), dto);
    }

    @Transactional
    public HitoResponseDTO crear(Long idProyecto, HitoCreateDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", idProyecto));
        Fase fase = faseRepository.findByIdAndProyectoId(dto.idFase(), idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Fase", dto.idFase()));
        List<Actividad> actividades = cargarActividades(fase, dto.idsActividades(), null);
        LocalDate fechaClave = calcularFechaClave(actividades);

        Hito hito = Hito.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaClave(fechaClave)
            .fechaCumplimientoReal(dto.fechaCumplimientoReal())
            .estado(EstadoHito.PENDIENTE)
            .proyecto(proyecto)
            .fase(fase)
            .build();
        Hito guardado = hitoRepository.save(hito);
        actividades.forEach(actividad -> actividad.setHito(guardado));
        actividadRepository.saveAll(actividades);
        avanceProyectoService.resumir(guardado);
        return toDTO(guardado);
    }

    @Transactional
    public HitoResponseDTO actualizar(Long idProyecto, Long id, HitoCreateDTO dto) {
        Hito hito = hitoRepository.findByIdAndProyectoId(id, idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));
        Fase fase = faseRepository.findByIdAndProyectoId(dto.idFase(), idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Fase", dto.idFase()));
        List<Actividad> nuevas = cargarActividades(fase, dto.idsActividades(), hito.getId());
        LocalDate nuevaFechaClave = calcularFechaClave(nuevas);

        cronogramaService.registrarSiCambio(
            TipoEntidadCronograma.HITO,
            hito.getId(),
            null,
            hito.getFechaClave(),
            null,
            nuevaFechaClave,
            dto.motivoReprogramacion()
        );

        Set<Long> nuevosIds = nuevas.stream().map(Actividad::getId)
            .collect(java.util.stream.Collectors.toSet());
        actividadRepository.findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(hito.getId())
            .stream()
            .filter(actual -> !nuevosIds.contains(actual.getId()))
            .forEach(actual -> actual.setHito(null));
        nuevas.forEach(actividad -> actividad.setHito(hito));

        hito.setNombre(dto.nombre());
        hito.setDescripcion(dto.descripcion());
        hito.setFechaClave(nuevaFechaClave);
        hito.setFase(fase);
        hito.setProyecto(fase.getProyecto());
        actividadRepository.flush();
        avanceProyectoService.resumir(hito);
        return toDTO(hito);
    }

    @Transactional
    public void eliminar(Long idProyecto, Long id) {
        Hito hito = hitoRepository.findByIdAndProyectoId(id, idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));
        actividadRepository.findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(id)
            .forEach(actividad -> actividad.setHito(null));
        actividadRepository.flush();
        hitoRepository.delete(hito);
    }

    @Transactional
    public HitoResponseDTO cambiarEstado(Long id, EstadoHito nuevoEstado) {
        Hito hito = hitoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));
        AvanceProyectoService.ResumenHito resumen = avanceProyectoService.resumir(hito);
        if (nuevoEstado != resumen.estado()) {
            throw new BusinessException("El estado del hito se calcula desde sus actividades");
        }
        return toDTO(hito);
    }

    @Transactional
    public void sincronizarDesdeActividades(Long hitoId, String motivo) {
        if (hitoId == null) return;
        Hito hito = hitoRepository.findById(hitoId)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", hitoId));
        List<Actividad> actividades =
            actividadRepository.findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(hitoId);
        if (actividades.isEmpty()) {
            throw new BusinessException("Un hito debe conservar al menos una actividad asociada");
        }
        LocalDate nuevaFechaClave = calcularFechaClave(actividades);
        cronogramaService.registrarSiCambio(
            TipoEntidadCronograma.HITO,
            hito.getId(),
            null,
            hito.getFechaClave(),
            null,
            nuevaFechaClave,
            motivo
        );
        hito.setFechaClave(nuevaFechaClave);
        avanceProyectoService.resumir(hito);
        hitoRepository.save(hito);
    }

    private List<Actividad> cargarActividades(Fase fase, Set<Long> ids, Long hitoActualId) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("Debe seleccionar al menos una actividad para el hito");
        }
        List<Actividad> actividades = actividadRepository.findAllById(new HashSet<>(ids));
        if (actividades.size() != ids.size()) {
            throw new BusinessException("Una o más actividades seleccionadas no existen");
        }
        for (Actividad actividad : actividades) {
            if (!actividad.getFase().getId().equals(fase.getId())) {
                throw new BusinessException("Todas las actividades del hito deben pertenecer a la misma fase");
            }
            if (actividad.getHito() != null
                && !actividad.getHito().getId().equals(hitoActualId)) {
                throw new BusinessException(
                    "La actividad '" + actividad.getNombre() + "' ya pertenece a otro hito"
                );
            }
            if (actividad.getFechaFinPlanificada() == null) {
                throw new BusinessException("Las actividades del hito deben tener fecha fin planificada");
            }
        }
        return actividades;
    }

    private LocalDate calcularFechaClave(List<Actividad> actividades) {
        return actividades.stream()
            .map(Actividad::getFechaFinPlanificada)
            .max(LocalDate::compareTo)
            .orElseThrow(() -> new BusinessException("No se pudo calcular la fecha clave del hito"));
    }

    private HitoResponseDTO toDTO(Hito hito) {
        AvanceProyectoService.ResumenHito resumen = avanceProyectoService.resumir(hito);
        var desfase = cronogramaService.calcular(
            hito.getFechaClave(),
            hito.getFechaCumplimientoReal(),
            resumen.estado() == EstadoHito.FINALIZADO
        );
        List<Actividad> actividades =
            actividadRepository.findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(hito.getId());
        return new HitoResponseDTO(
            hito.getId(),
            hito.getNombre(),
            hito.getDescripcion(),
            hito.getFechaClave(),
            resumen.estado(),
            hito.getProyecto().getId(),
            hito.getFase().getId(),
            hito.getFase().getNombre(),
            actividades.stream().map(Actividad::getId).toList(),
            resumen.porcentajeAvance(),
            resumen.fechaInicioPlanificada(),
            resumen.fechaFinPlanificada(),
            resumen.duracionDias(),
            resumen.totalActividades(),
            resumen.actividadesFinalizadas(),
            hito.getFechaCumplimientoReal(),
            desfase.dias(),
            desfase.estado(),
            cronogramaService.listar(TipoEntidadCronograma.HITO, hito.getId()),
            hito.getFechaCreacion(),
            hito.getFechaModificacion()
        );
    }

    private void validarProyectoExiste(Long idProyecto) {
        if (!proyectoRepository.existsById(idProyecto)) {
            throw new ResourceNotFoundException("Proyecto", idProyecto);
        }
    }
}
