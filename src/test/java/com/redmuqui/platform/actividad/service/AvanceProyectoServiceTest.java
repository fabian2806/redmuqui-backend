package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.entity.Fase;
import com.redmuqui.platform.actividad.entity.EstadoFase;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.FaseRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvanceProyectoServiceTest {

    @Mock private ActividadRepository actividadRepository;
    @Mock private HitoRepository hitoRepository;
    @Mock private FaseRepository faseRepository;
    @Mock private SubactividadRepository subactividadRepository;
    @Mock private ProyectoRepository proyectoRepository;

    private AvanceProyectoService service;

    @BeforeEach
    void setUp() {
        service = new AvanceProyectoService(
            actividadRepository,
            hitoRepository,
            faseRepository,
            subactividadRepository,
            proyectoRepository
        );
    }

    @Test
    void calculaAvanceDelHitoPonderandoDuracionDeActividades() {
        Hito hito = Hito.builder().id(10L).build();
        Actividad cortaFinalizada = actividad(1L, "2026-06-01", "2026-06-02", EstadoActividad.FINALIZADA);
        Actividad largaPendiente = actividad(2L, "2026-06-03", "2026-06-08", EstadoActividad.PENDIENTE);
        when(actividadRepository.findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(10L))
            .thenReturn(List.of(cortaFinalizada, largaPendiente));

        AvanceProyectoService.ResumenHito resumen = service.resumir(hito);

        assertThat(resumen.porcentajeAvance()).isEqualTo(25D);
        assertThat(resumen.duracionDias()).isEqualTo(8L);
        assertThat(resumen.actividadesFinalizadas()).isEqualTo(1);
        assertThat(resumen.estado()).isEqualTo(EstadoHito.EN_CURSO);
    }

    @Test
    void calculaAvanceDelProyectoPonderandoDuracionDeFases() {
        Proyecto proyecto = Proyecto.builder().id(1L).porcentajeAvance(0D).build();
        Fase faseCorta = Fase.builder().id(10L)
            .fechaInicioPlanificada(LocalDate.parse("2026-06-01"))
            .fechaFinPlanificada(LocalDate.parse("2026-06-02"))
            .estado(EstadoFase.PENDIENTE).build();
        Fase faseLarga = Fase.builder().id(20L)
            .fechaInicioPlanificada(LocalDate.parse("2026-06-03"))
            .fechaFinPlanificada(LocalDate.parse("2026-06-08"))
            .estado(EstadoFase.PENDIENTE).build();
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));
        when(faseRepository.findByProyectoIdOrderByFechaInicioPlanificadaAscIdAsc(1L))
            .thenReturn(List.of(faseCorta, faseLarga));
        when(actividadRepository.findByFaseIdOrderByFechaInicioPlanificadaAscIdAsc(10L)).thenReturn(List.of(
            actividad(1L, "2026-06-01", "2026-06-02", EstadoActividad.FINALIZADA)
        ));
        when(actividadRepository.findByFaseIdOrderByFechaInicioPlanificadaAscIdAsc(20L)).thenReturn(List.of(
            actividad(2L, "2026-06-03", "2026-06-08", EstadoActividad.PENDIENTE)
        ));

        service.recalcularProyecto(1L);

        assertThat(proyecto.getPorcentajeAvance()).isEqualTo(25D);
        assertThat(faseCorta.getEstado()).isEqualTo(EstadoFase.FINALIZADA);
        assertThat(faseLarga.getEstado()).isEqualTo(EstadoFase.PENDIENTE);
        verify(proyectoRepository).save(proyecto);
    }

    private Actividad actividad(Long id, String inicio, String fin, EstadoActividad estado) {
        return Actividad.builder()
            .id(id)
            .fechaInicioPlanificada(LocalDate.parse(inicio))
            .fechaFinPlanificada(LocalDate.parse(fin))
            .estado(estado)
            .build();
    }
}
