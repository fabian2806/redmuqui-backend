package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.ActividadCreateDTO;
import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.dto.ActividadUpdateDTO;
import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.entity.TipoEntidadCronograma;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.FaseRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.documento.dto.DocumentoEntregableResponseDTO;
import com.redmuqui.platform.documento.entity.TipoVinculoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redmuqui.platform.actividad.dto.SubactividadArchivoResponseDTO;
import com.redmuqui.platform.actividad.dto.SubactividadCofinanciamientoResponseDTO;
import com.redmuqui.platform.actividad.dto.SubactividadResponseDTO;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.proyecto.entity.Proyecto;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HitoRepository hitoRepository;
    private final FaseRepository faseRepository;
    private final AvanceProyectoService avanceProyectoService;
    private final HitoService hitoService;
    private final CofinanciamientoService cofinanciamientoService;
    private final CronogramaService cronogramaService;
    private final ValidacionCronogramaService validacionCronogramaService;
    private final DocumentoRepository documentoRepository;

    @Transactional(readOnly = true)
    public Page<ActividadResponseDTO> listar(Long proyectoId, Pageable pageable) {
        if (proyectoId != null) {
            return actividadRepository.findByProyectoId(proyectoId, pageable).map(this::toDTO);
        }
        return actividadRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ActividadResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public List<ActividadResponseDTO> listarPorProyecto(Long proyectoId) {
        return actividadRepository.findByProyectoId(proyectoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActividadResponseDTO crear(ActividadCreateDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(dto.idProyecto())
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto()));
        var fase = faseRepository.findByIdAndProyectoId(dto.idFase(), dto.idProyecto())
            .orElseThrow(() -> new ResourceNotFoundException("Fase", dto.idFase()));
        validacionCronogramaService.validarActividadEnFase(
            fase, dto.fechaInicioPlanificada(), dto.fechaFinPlanificada()
        );
        validacionCronogramaService.validarFechaReal(
            dto.fechaFinReal(), dto.fechaInicioReal(), "la actividad"
        );
        Hito hito = buscarHitoOpcional(dto.idHito(), fase.getId());
        double presupuestoActividad = dto.presupuesto() != null ? dto.presupuesto() : 0.0;
        validarPresupuestoDentroDelProyecto(proyecto, presupuestoActividad, null);
        Actividad actividad = Actividad.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaInicioPlanificada(dto.fechaInicioPlanificada())
            .fechaFinPlanificada(dto.fechaFinPlanificada())
            .fechaInicioReal(dto.fechaInicioReal())
            .fechaFinReal(dto.fechaFinReal())
            .estado(dto.estado() != null ? dto.estado() : EstadoActividad.PENDIENTE)
            .porcentajeAvance(dto.estado() == EstadoActividad.FINALIZADA ? 100 : 0)
            .presupuesto(presupuestoActividad)
            .proyecto(proyecto)
            .fase(fase)
            .hito(hito)
            .build();

        if (dto.idResponsables() != null && !dto.idResponsables().isEmpty()) {
            actividad.setResponsables(new HashSet<>(usuarioRepository.findAllById(dto.idResponsables())));
        }

        Actividad guardada = actividadRepository.save(actividad);
        if (hito != null) {
            hitoService.sincronizarDesdeActividades(hito.getId(), "Actividad asociada al hito");
        }
        avanceProyectoService.recalcularProyecto(proyecto.getId());
        return toDTO(guardada);
    }

    @Transactional
    public ActividadResponseDTO actualizar(Long id, ActividadUpdateDTO dto) {
        Actividad a = buscarOFallar(id);
        Long proyectoAnteriorId = a.getProyecto().getId();
        Long hitoAnteriorId = a.getHito() != null ? a.getHito().getId() : null;
        Proyecto proyecto = proyectoRepository.findById(dto.idProyecto())
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto()));
        var fase = faseRepository.findByIdAndProyectoId(dto.idFase(), dto.idProyecto())
            .orElseThrow(() -> new ResourceNotFoundException("Fase", dto.idFase()));
        validacionCronogramaService.validarCambioRangoActividad(
            a, fase, dto.fechaInicioPlanificada(), dto.fechaFinPlanificada()
        );
        validacionCronogramaService.validarFechaReal(
            dto.fechaFinReal(), dto.fechaInicioReal(), "la actividad"
        );
        Hito hito = buscarHitoOpcional(dto.idHito(), fase.getId());
        Long hitoNuevoId = hito != null ? hito.getId() : null;
        validarQueHitoConserveActividades(hitoAnteriorId, hitoNuevoId);

        cronogramaService.registrarSiCambio(
            TipoEntidadCronograma.ACTIVIDAD,
            a.getId(),
            a.getFechaInicioPlanificada(),
            a.getFechaFinPlanificada(),
            dto.fechaInicioPlanificada(),
            dto.fechaFinPlanificada(),
            dto.motivoReprogramacion()
        );
        a.setNombre(dto.nombre());
        a.setDescripcion(dto.descripcion());
        a.setFechaInicioPlanificada(dto.fechaInicioPlanificada());
        a.setFechaFinPlanificada(dto.fechaFinPlanificada());
        a.setFechaInicioReal(dto.fechaInicioReal());
        a.setFechaFinReal(dto.fechaFinReal());
        double presupuestoActualizado = dto.presupuesto() != null ? dto.presupuesto() : 0.0;
        validarPresupuestoNoMenorAlComprometido(a, presupuestoActualizado);
        validarPresupuestoDentroDelProyecto(proyecto, presupuestoActualizado, a.getId());
        a.setPresupuesto(presupuestoActualizado);
        if (dto.estado() != null) {
            a.setEstado(dto.estado());
            a.setPorcentajeAvance(dto.estado() == EstadoActividad.FINALIZADA ? 100 : 0);
            if (dto.estado() == EstadoActividad.EN_CURSO && a.getFechaInicioReal() == null) {
                a.setFechaInicioReal(LocalDate.now());
            }
            if (dto.estado() == EstadoActividad.FINALIZADA && a.getFechaFinReal() == null) {
                a.setFechaFinReal(LocalDate.now());
            }
        }
        a.setProyecto(proyecto);
        a.setFase(fase);
        a.setHito(hito);

        if (dto.idResponsables() != null) {
            a.setResponsables(new HashSet<>(usuarioRepository.findAllById(dto.idResponsables())));
        }

        Actividad guardada = actividadRepository.save(a);
        actividadRepository.flush();
        if (hitoAnteriorId != null && !hitoAnteriorId.equals(hitoNuevoId)) {
            hitoService.sincronizarDesdeActividades(hitoAnteriorId, dto.motivoReprogramacion());
        }
        if (hitoNuevoId != null) {
            hitoService.sincronizarDesdeActividades(hitoNuevoId, dto.motivoReprogramacion());
        }
        avanceProyectoService.recalcularProyecto(proyectoAnteriorId);
        if (!proyectoAnteriorId.equals(proyecto.getId())) avanceProyectoService.recalcularProyecto(proyecto.getId());
        return toDTO(guardada);
    }

    @Transactional
    public ActividadResponseDTO cambiarEstado(Long id, EstadoActividad nuevoEstado) {
        Actividad a = buscarOFallar(id);
        Long hitoId = a.getHito() != null ? a.getHito().getId() : null;
        a.setEstado(nuevoEstado);
        a.setPorcentajeAvance(nuevoEstado == EstadoActividad.FINALIZADA ? 100 : 0);
        if (nuevoEstado == EstadoActividad.EN_CURSO && a.getFechaInicioReal() == null) {
            a.setFechaInicioReal(LocalDate.now());
        }
        if (nuevoEstado == EstadoActividad.FINALIZADA && a.getFechaFinReal() == null) {
            a.setFechaFinReal(LocalDate.now());
        }
        Actividad guardada = actividadRepository.save(a);
        if (hitoId != null) hitoService.sincronizarDesdeActividades(hitoId, null);
        avanceProyectoService.recalcularProyecto(a.getProyecto().getId());
        return toDTO(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        Actividad a = buscarOFallar(id);
        Long proyectoId = a.getProyecto().getId();
        Long hitoId = a.getHito() != null ? a.getHito().getId() : null;
        validarQueHitoConserveActividades(hitoId, null);
        actividadRepository.delete(a);
        actividadRepository.flush();
        if (hitoId != null) {
            hitoService.sincronizarDesdeActividades(hitoId, "Actividad eliminada del hito");
        }
        avanceProyectoService.recalcularProyecto(proyectoId);
    }

    @Transactional
    public ActividadResponseDTO actualizarAvance(Long id, Integer porcentajeAvance) {
        Actividad a = buscarOFallar(id);
        int avance = porcentajeAvance == null ? 0 : Math.max(0, Math.min(100, porcentajeAvance));
        a.setPorcentajeAvance(avance);
        a.setEstado(avance >= 100 ? EstadoActividad.FINALIZADA : avance > 0 ? EstadoActividad.EN_CURSO : EstadoActividad.PENDIENTE);
        Actividad guardada = actividadRepository.save(a);
        if (a.getHito() != null) hitoService.sincronizarDesdeActividades(a.getHito().getId(), null);
        avanceProyectoService.recalcularProyecto(a.getProyecto().getId());
        return toDTO(guardada);
    }

    private void validarQueHitoConserveActividades(Long hitoAnteriorId, Long hitoNuevoId) {
        if (hitoAnteriorId == null || hitoAnteriorId.equals(hitoNuevoId)) return;
        int total = actividadRepository
            .findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(hitoAnteriorId)
            .size();
        if (total <= 1) {
            throw new BusinessException(
                "No se puede retirar la última actividad del hito; modifica o elimina primero el hito"
            );
        }
    }

    private Hito buscarHitoOpcional(Long idHito, Long idFase) {
        if (idHito == null) return null;
        Hito hito = hitoRepository.findById(idHito)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", idHito));
        if (!hito.getFase().getId().equals(idFase)) {
            throw new com.redmuqui.platform.common.exception.BusinessException(
                "El hito y la actividad deben pertenecer a la misma fase"
            );
        }
        return hito;
    }

    private Actividad buscarOFallar(Long id) {
        return actividadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad", id));
    }

    private void validarPresupuestoNoMenorAlComprometido(Actividad actividad, double presupuesto) {
        double comprometido = cofinanciamientoService.calcularPresupuestoComprometido(actividad);
        if (presupuesto < comprometido) {
            throw new BusinessException(
                "El presupuesto de la actividad no puede ser menor al monto comprometido. Comprometido: "
                    + comprometido
            );
        }
    }

    private void validarPresupuestoDentroDelProyecto(
            Proyecto proyecto,
            double presupuestoActividad,
            Long actividadIdActual) {
        double presupuestoProyecto = proyecto.getPresupuesto() == null ? 0D : proyecto.getPresupuesto();
        double presupuestoOtrasActividades = actividadIdActual == null
            ? actividadRepository.sumPresupuestoByProyectoId(proyecto.getId())
            : actividadRepository.sumPresupuestoByProyectoIdExcludingActividad(proyecto.getId(), actividadIdActual);
        double totalAsignado = presupuestoOtrasActividades + presupuestoActividad;
        if (totalAsignado > presupuestoProyecto) {
            throw new BusinessException(
                "El presupuesto asignado a actividades excede el presupuesto del proyecto. Disponible: "
                    + (presupuestoProyecto - presupuestoOtrasActividades)
            );
        }
    }

    private ActividadResponseDTO toDTO(Actividad a) {
        double presupuesto = a.getPresupuesto() == null ? 0D : a.getPresupuesto();
        double presupuestoDisponible = cofinanciamientoService.calcularPresupuestoDisponible(a);
        double costoEstimado = presupuesto;
        double costoReal = a.getSubactividades() == null ? 0D : a.getSubactividades().stream()
            .map(Subactividad::getCostoReal)
            .filter(java.util.Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .sum();
        var desfase = cronogramaService.calcular(
            a.getFechaFinPlanificada(),
            a.getFechaFinReal(),
            a.getEstado() == EstadoActividad.FINALIZADA
        );
        return new ActividadResponseDTO(
            a.getId(), a.getNombre(), a.getDescripcion(),
            a.getFechaInicioPlanificada(), a.getFechaFinPlanificada(), a.getEstado(),
            a.getPorcentajeAvance(),
            calcularAvancePlanificado(a.getFechaInicioPlanificada(), a.getFechaFinPlanificada()),
            presupuesto,
            presupuestoDisponible,
            costoEstimado,
            costoReal,
            a.getProyecto().getMoneda(),
            a.getFechaInicioReal(),
            a.getFechaFinReal(),
            desfase.dias(),
            desfase.estado(),
            cronogramaService.listar(TipoEntidadCronograma.ACTIVIDAD, a.getId()),
            a.getProyecto().getId(),
            a.getFase().getId(),
            a.getFase().getNombre(),
            a.getHito() != null ? a.getHito().getId() : null,
            a.getHito() != null ? a.getHito().getNombre() : null,
            a.getResponsables().stream().map(u -> u.getId()).collect(Collectors.toSet()),
            a.getSubactividades() == null ? List.of() : a.getSubactividades().stream()
                .map(this::mapSubactividad)
                .collect(Collectors.toList())
        );
    }

    private SubactividadResponseDTO mapSubactividad(Subactividad s) {
        var desfase = cronogramaService.calcular(
            s.getFechaFinPlanificada(),
            s.getFechaFinReal(),
            s.getEstado() == com.redmuqui.platform.actividad.entity.EstadoSubactividad.FINALIZADA
        );
        return new SubactividadResponseDTO(
            s.getId(),
            s.getNombre(),
            s.getResponsable().getNombres() + " " + s.getResponsable().getApellidos(),
            s.getPresupuesto(),
            s.getCostoReal(),
            s.getActividad().getProyecto().getMoneda(),
            s.getPorcentajeAvance(),
            calcularAvancePlanificado(s.getFechaInicioPlanificada(), s.getFechaFinPlanificada()),
            s.getHombresInvolucrados(),
            s.getMujeresInvolucradas(),
            s.getFechaInicioPlanificada(),
            s.getFechaFinPlanificada(),
            s.getFechaInicioReal(),
            s.getFechaFinReal(),
            desfase.dias(),
            desfase.estado(),
            s.getEstado(),
            s.getDescripcion(),
            documentoRepository.findBySubactividadIdOrderByFechaCargaDescIdDesc(s.getId()).stream()
                .filter(d -> d.getTipoVinculo() == TipoVinculoDocumento.ENTREGABLE_FINAL)
                .map(d -> new DocumentoEntregableResponseDTO(
                    d.getId(),
                    d.getTitulo(),
                    d.getEstado(),
                    d.getVersion(),
                    d.getFechaCarga(),
                    d.getUsuarioCarga() != null
                        ? (d.getUsuarioCarga().getNombres() + " "
                            + d.getUsuarioCarga().getApellidos()).trim()
                        : null
                ))
                .toList(),
            s.getArchivosEvidencia() == null ? List.of() : s.getArchivosEvidencia().stream()
                .map(a -> new SubactividadArchivoResponseDTO(
                    a.getId(), a.getNombre(), a.getUrl(), a.getEstado(),
                    a.getUsuarioCarga().getId(),
                    (a.getUsuarioCarga().getNombres() + " " + a.getUsuarioCarga().getApellidos()).trim()))
                .collect(Collectors.toList()),
            s.getCofinanciamientos() == null ? List.of() : s.getCofinanciamientos().stream()
                .map(c -> new SubactividadCofinanciamientoResponseDTO(
                    c.getActividadOrigen().getId(),
                    c.getActividadOrigen().getNombre(),
                    c.getActividadOrigen().getProyecto().getId(),
                    c.getActividadOrigen().getProyecto().getNombre(),
                    c.getMonto(),
                    c.getJustificacion()
                ))
                .collect(Collectors.toList()),
            cronogramaService.listar(TipoEntidadCronograma.SUBACTIVIDAD, s.getId()),
            s.getActividad().getId(),
            s.getActividad().getProyecto().getId()
        );
    }

    private double calcularAvancePlanificado(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) return 0D;
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(inicio)) return 0D;
        if (!hoy.isBefore(fin)) return 100D;
        long total = Math.max(1, ChronoUnit.DAYS.between(inicio, fin));
        return ChronoUnit.DAYS.between(inicio, hoy) * 100D / total;
    }
}
