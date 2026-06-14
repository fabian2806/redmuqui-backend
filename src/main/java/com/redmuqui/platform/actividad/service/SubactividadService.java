package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.*;
import com.redmuqui.platform.actividad.entity.*;
import com.redmuqui.platform.actividad.repository.*;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import com.redmuqui.platform.documento.dto.DocumentoEntregableResponseDTO;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.entity.TipoVinculoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SubactividadService {

    private final SubactividadRepository subactividadRepository;
    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final SubactividadCofinanciamientoRepository cofinanciamientoRepository;
    private final SubactividadArchivoRepository archivoRepository;
    private final AvanceProyectoService avanceProyectoService;
    private final AuthenticatedUserService authenticatedUserService;
    private final CronogramaService cronogramaService;
    private final ValidacionCronogramaService validacionCronogramaService;
    private final DocumentoRepository documentoRepository;
    
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Transactional
    public SubactividadResponseDTO crear(Long actividadId, SubactividadCreateDTO dto) {
        Actividad actividad = actividadRepository.findById(actividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad", actividadId));
        validacionCronogramaService.validarSubactividadEnActividad(
            actividad, dto.fechaInicioPlanificada(), dto.fechaFinPlanificada()
        );
        EstadoSubactividad estado = dto.estado() == EstadoSubactividad.FINALIZADA
            ? EstadoSubactividad.PENDIENTE
            : dto.estado() != null ? dto.estado() : EstadoSubactividad.PENDIENTE;
        LocalDate fechaInicioReal = estado == EstadoSubactividad.EN_CURSO
            ? dto.fechaInicioReal() != null ? dto.fechaInicioReal() : dto.fechaInicioPlanificada()
            : null;
        LocalDate fechaFinReal = null;
        validacionCronogramaService.validarFechaReal(
            fechaFinReal, fechaInicioReal, dto.fechaInicioPlanificada(), "la subactividad"
        );
            
        Subactividad subactividad = Subactividad.builder()
            .nombre(dto.nombre())
            .responsable(usuarioRepository.findById(dto.idResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsable())))
            .presupuesto(dto.presupuesto() != null ? dto.presupuesto() : 0.0)
            .costoReal(dto.costoReal())
            .porcentajeAvance(0)
            .hombresInvolucrados(dto.hombresInvolucrados() != null ? dto.hombresInvolucrados() : 0)
            .mujeresInvolucradas(dto.mujeresInvolucradas() != null ? dto.mujeresInvolucradas() : 0)
            .fechaInicioPlanificada(dto.fechaInicioPlanificada())
            .fechaFinPlanificada(dto.fechaFinPlanificada())
            .fechaInicioReal(fechaInicioReal)
            .fechaFinReal(fechaFinReal)
            .estado(estado)
            .descripcion(dto.descripcion())
            .actividad(actividad)
            .build();

        Subactividad guardada = subactividadRepository.save(subactividad);
        avanceProyectoService.recalcularActividad(actividad.getId());
        return toDTO(guardada);
    }

    @Transactional
    public SubactividadResponseDTO actualizar(Long subactividadId, SubactividadCreateDTO dto) {
        Subactividad s = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));

        cronogramaService.registrarSiCambio(
            TipoEntidadCronograma.SUBACTIVIDAD,
            s.getId(),
            s.getFechaInicioPlanificada(),
            s.getFechaFinPlanificada(),
            dto.fechaInicioPlanificada(),
            dto.fechaFinPlanificada(),
            dto.motivoReprogramacion()
        );
        validacionCronogramaService.validarSubactividadEnActividad(
            s.getActividad(), dto.fechaInicioPlanificada(), dto.fechaFinPlanificada()
        );
        s.setNombre(dto.nombre());
        s.setDescripcion(dto.descripcion());
        if (dto.idResponsable() != null) {
            s.setResponsable(usuarioRepository.findById(dto.idResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsable())));
        }
        s.setPresupuesto(dto.presupuesto() != null ? dto.presupuesto() : 0.0);
        s.setCostoReal(dto.costoReal());
        s.setHombresInvolucrados(dto.hombresInvolucrados() != null ? dto.hombresInvolucrados() : 0);
        s.setMujeresInvolucradas(dto.mujeresInvolucradas() != null ? dto.mujeresInvolucradas() : 0);
        s.setFechaInicioPlanificada(dto.fechaInicioPlanificada());
        s.setFechaFinPlanificada(dto.fechaFinPlanificada());
        EstadoSubactividad nuevoEstado = dto.estado() != null ? dto.estado() : EstadoSubactividad.PENDIENTE;
        LocalDate fechaInicioReal = dto.fechaInicioReal() != null
            ? dto.fechaInicioReal()
            : s.getFechaInicioReal();
        LocalDate fechaFinReal = dto.fechaFinReal() != null
            ? dto.fechaFinReal()
            : s.getFechaFinReal();

        if (nuevoEstado == EstadoSubactividad.PENDIENTE) {
            fechaInicioReal = null;
            fechaFinReal = null;
            s.setPorcentajeAvance(0);
        } else if (nuevoEstado == EstadoSubactividad.EN_CURSO) {
            if (fechaInicioReal == null || fechaInicioReal.isBefore(dto.fechaInicioPlanificada())) {
                fechaInicioReal = dto.fechaInicioPlanificada();
            }
            fechaFinReal = null;
        }
        if (nuevoEstado == EstadoSubactividad.FINALIZADA) {
            validarCierre(s, dto.costoReal());
            if (fechaInicioReal == null || fechaInicioReal.isBefore(dto.fechaInicioPlanificada())) {
                fechaInicioReal = dto.fechaInicioPlanificada();
            }
            if (fechaFinReal == null) {
                throw new BusinessException("Debe registrar la fecha real de finalización");
            }
            s.setPorcentajeAvance(100);
        }
        validacionCronogramaService.validarFechaReal(
            fechaFinReal, fechaInicioReal, dto.fechaInicioPlanificada(), "la subactividad"
        );
        s.setFechaInicioReal(fechaInicioReal);
        s.setFechaFinReal(fechaFinReal);
        s.setEstado(nuevoEstado);

        Subactividad guardada = subactividadRepository.save(s);
        avanceProyectoService.recalcularActividad(guardada.getActividad().getId());
        return toDTO(guardada);
    }

    @Transactional
    public void eliminar(Long subactividadId) {
        Subactividad s = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));
        Long actividadId = s.getActividad().getId();
        subactividadRepository.delete(s);
        subactividadRepository.flush();
        avanceProyectoService.recalcularActividad(actividadId);
    }

    @Transactional
    public SubactividadResponseDTO cofinanciar(Long subactividadId, SubactividadCofinanciamientoCreateDTO dto) {
        Subactividad subactividad = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));
            
        Actividad actividadOrigen = actividadRepository.findById(dto.actividadId())
            .orElseThrow(() -> new ResourceNotFoundException("Actividad", dto.actividadId()));
        String monedaDestino = subactividad.getActividad().getProyecto().getMoneda();
        String monedaOrigen = actividadOrigen.getProyecto().getMoneda();
        if (!java.util.Objects.equals(monedaDestino, monedaOrigen)) {
            throw new BusinessException(
                "No se puede cofinanciar entre proyectos con monedas diferentes ("
                    + monedaOrigen + " y " + monedaDestino + ")"
            );
        }
            
        SubactividadCofinanciamiento cofinanciamiento = SubactividadCofinanciamiento.builder()
            .subactividad(subactividad)
            .actividadOrigen(actividadOrigen)
            .monto(dto.monto())
            .build();
            
        // Check if cofinanciamiento already exists for this pair
        SubactividadCofinanciamientoId id = new SubactividadCofinanciamientoId(subactividadId, dto.actividadId());
        if (cofinanciamientoRepository.existsById(id)) {
            SubactividadCofinanciamiento existing = cofinanciamientoRepository.findById(id).get();
            existing.setMonto(existing.getMonto() + dto.monto());
            cofinanciamientoRepository.save(existing);
        } else {
            cofinanciamiento.setId(id);
            subactividad.addCofinanciamiento(cofinanciamiento);
            cofinanciamientoRepository.save(cofinanciamiento);
        }
        
        return toDTO(subactividadRepository.save(subactividad));
    }
    
    @Transactional
    public SubactividadResponseDTO subirEvidencia(Long subactividadId, MultipartFile file, Integer hombres, Integer mujeres) {
        Subactividad subactividad = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));
            
        if (hombres != null) subactividad.setHombresInvolucrados(subactividad.getHombresInvolucrados() + hombres);
        if (mujeres != null) subactividad.setMujeresInvolucradas(subactividad.getMujeresInvolucradas() + mujeres);
            
        if (file != null && !file.isEmpty()) {
            try {
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, file.getBytes());
                
                SubactividadArchivo archivo = SubactividadArchivo.builder()
                    .nombre(file.getOriginalFilename())
                    .url("/uploads/" + fileName)
                    .estado(EstadoEvidencia.EN_REVISION)
                    .usuarioCarga(authenticatedUserService.obtenerUsuario())
                    .subactividad(subactividad)
                    .build();
                    
                subactividad.addArchivo(archivo);
                archivoRepository.save(archivo);
                subactividad.setEstado(EstadoSubactividad.EN_CURSO);
                if (subactividad.getFechaInicioReal() == null) {
                    subactividad.setFechaInicioReal(subactividad.getFechaInicioPlanificada());
                }
                subactividad.setPorcentajeAvance(calcularAvanceEvidencias(subactividad));
            } catch (IOException e) {
                throw new RuntimeException("No se pudo guardar el archivo localmente", e);
            }
        }
        
        Subactividad guardada = subactividadRepository.save(subactividad);
        avanceProyectoService.recalcularActividad(guardada.getActividad().getId());
        return toDTO(guardada);
    }

    @Transactional
    public SubactividadResponseDTO cambiarEstadoEvidencia(
            Long subactividadId, Long archivoId, EstadoEvidencia estado) {
        SubactividadArchivo archivo = archivoRepository.findByIdAndSubactividadId(archivoId, subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Evidencia", archivoId));
        archivo.setEstado(estado);
        archivoRepository.save(archivo);
        Subactividad subactividad = archivo.getSubactividad();
        subactividad.setPorcentajeAvance(calcularAvanceEvidencias(subactividad));
        if (subactividad.getEstado() == EstadoSubactividad.PENDIENTE) {
            subactividad.setEstado(EstadoSubactividad.EN_CURSO);
            if (subactividad.getFechaInicioReal() == null) {
                subactividad.setFechaInicioReal(subactividad.getFechaInicioPlanificada());
            }
        }
        Subactividad guardada = subactividadRepository.save(subactividad);
        avanceProyectoService.recalcularActividad(guardada.getActividad().getId());
        return toDTO(guardada);
    }

    public SubactividadResponseDTO toDTO(Subactividad s) {
        var desfase = cronogramaService.calcular(
            s.getFechaFinPlanificada(),
            s.getFechaFinReal(),
            s.getEstado() == EstadoSubactividad.FINALIZADA
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
            s.getArchivosEvidencia().stream()
                .map(a -> new SubactividadArchivoResponseDTO(
                    a.getId(), a.getNombre(), a.getUrl(), a.getEstado(),
                    a.getUsuarioCarga().getId(),
                    (a.getUsuarioCarga().getNombres() + " " + a.getUsuarioCarga().getApellidos()).trim()))
                .collect(Collectors.toList()),
            s.getCofinanciamientos().stream()
                .map(c -> new SubactividadCofinanciamientoResponseDTO(c.getActividadOrigen().getId(), c.getMonto()))
                .collect(Collectors.toList()),
            cronogramaService.listar(TipoEntidadCronograma.SUBACTIVIDAD, s.getId()),
            s.getActividad().getId(),
            s.getActividad().getProyecto().getId()
        );
    }

    private void validarCierre(Subactividad subactividad, Double costoReal) {
        if (costoReal == null) {
            throw new BusinessException("Debe registrar el costo real para finalizar la subactividad");
        }
        boolean tieneEvidenciaAceptada = subactividad.getArchivosEvidencia().stream()
            .anyMatch(a -> a.getEstado() == EstadoEvidencia.ACEPTADO);
        boolean tieneEntregablePublicado =
            documentoRepository.findBySubactividadIdOrderByFechaCargaDescIdDesc(subactividad.getId())
                .stream()
                .anyMatch(d -> d.getTipoVinculo() == TipoVinculoDocumento.ENTREGABLE_FINAL
                    && d.getEstado() == EstadoDocumento.PUBLICADO);
        if (!tieneEvidenciaAceptada && !tieneEntregablePublicado) {
            throw new BusinessException(
                "Debe existir un entregable publicado para finalizar la subactividad");
        }
    }

    private int calcularAvanceEvidencias(Subactividad subactividad) {
        if (subactividad.getArchivosEvidencia().isEmpty()) return 0;
        long aceptadas = subactividad.getArchivosEvidencia().stream()
            .filter(a -> a.getEstado() == EstadoEvidencia.ACEPTADO).count();
        long revision = subactividad.getArchivosEvidencia().stream()
            .filter(a -> a.getEstado() == EstadoEvidencia.EN_REVISION).count();
        if (aceptadas == subactividad.getArchivosEvidencia().size()) return 100;
        if (aceptadas > 0) return 75;
        if (revision > 0) return 50;
        return 25;
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
