package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.CronogramaReprogramacionDTO;
import com.redmuqui.platform.actividad.entity.CronogramaReprogramacion;
import com.redmuqui.platform.actividad.entity.EstadoCronograma;
import com.redmuqui.platform.actividad.entity.TipoEntidadCronograma;
import com.redmuqui.platform.actividad.repository.CronogramaReprogramacionRepository;
import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import com.redmuqui.platform.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CronogramaService {

    private final CronogramaReprogramacionRepository repository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public void registrarSiCambio(
        TipoEntidadCronograma tipo,
        Long idEntidad,
        LocalDate inicioAnterior,
        LocalDate finAnterior,
        LocalDate inicioNueva,
        LocalDate finNueva,
        String motivo
    ) {
        if (Objects.equals(inicioAnterior, inicioNueva) && Objects.equals(finAnterior, finNueva)) {
            return;
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessException("Debe indicar el motivo de la reprogramación");
        }
        repository.save(CronogramaReprogramacion.builder()
            .tipoEntidad(tipo)
            .idEntidad(idEntidad)
            .fechaInicioAnterior(inicioAnterior)
            .fechaFinAnterior(finAnterior)
            .fechaInicioNueva(inicioNueva)
            .fechaFinNueva(finNueva)
            .motivo(motivo.trim())
            .usuario(authenticatedUserService.obtenerUsuario())
            .fechaCreacion(LocalDateTime.now())
            .build());
    }

    @Transactional(readOnly = true)
    public List<CronogramaReprogramacionDTO> listar(TipoEntidadCronograma tipo, Long idEntidad) {
        return repository.findByTipoEntidadAndIdEntidadOrderByFechaCreacionDesc(tipo, idEntidad)
            .stream()
            .map(item -> new CronogramaReprogramacionDTO(
                item.getId(),
                item.getFechaInicioAnterior(),
                item.getFechaFinAnterior(),
                item.getFechaInicioNueva(),
                item.getFechaFinNueva(),
                item.getMotivo(),
                item.getUsuario().getId(),
                (item.getUsuario().getNombres() + " " + item.getUsuario().getApellidos()).trim(),
                item.getFechaCreacion()
            ))
            .toList();
    }

    public DesfaseCronograma calcular(LocalDate fechaPlanificada, LocalDate fechaReal, boolean finalizado) {
        if (fechaPlanificada == null) {
            return new DesfaseCronograma(null, EstadoCronograma.PENDIENTE);
        }
        LocalDate fechaComparacion = fechaReal != null
            ? fechaReal
            : finalizado ? LocalDate.now() : null;
        if (fechaComparacion == null && LocalDate.now().isAfter(fechaPlanificada)) {
            fechaComparacion = LocalDate.now();
        }
        if (fechaComparacion == null) {
            return new DesfaseCronograma(null, EstadoCronograma.PENDIENTE);
        }

        long dias = ChronoUnit.DAYS.between(fechaPlanificada, fechaComparacion);
        EstadoCronograma estado = dias > 0
            ? EstadoCronograma.ATRASADO
            : dias < 0 ? EstadoCronograma.ADELANTADO : EstadoCronograma.EN_FECHA;
        return new DesfaseCronograma(dias, estado);
    }

    public record DesfaseCronograma(Long dias, EstadoCronograma estado) {}
}
