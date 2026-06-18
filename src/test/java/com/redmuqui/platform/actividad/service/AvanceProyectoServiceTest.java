package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
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
    @Mock private SubactividadRepository subactividadRepository;
    @Mock private ProyectoRepository proyectoRepository;

    private AvanceProyectoService service;

    @BeforeEach
    void setUp() {
        service = new AvanceProyectoService(
            actividadRepository,
            hitoRepository,
            subactividadRepository,
            proyectoRepository
        );
    }

    @Test
    void calculaAvanceDelHitoPonderandoDuracionDeActividades() {
        Hito hito = Hito.builder().id(10L).build();
        Actividad cortaFinalizada = actividad(1L, "2026-06-01", "2026-06-02", EstadoActividad.FINALIZADA);
        Actividad largaPendiente = actividad(2L, "2026-06-03", "2026-06-08", EstadoActividad.PENDIENTE);
        when(actividadRepository.findByHitoIdOrderByFechaInicioAscIdAsc(10L))
            .thenReturn(List.of(cortaFinalizada, largaPendiente));

        AvanceProyectoService.ResumenHito resumen = service.resumir(hito);

        assertThat(resumen.porcentajeAvance()).isEqualTo(25D);
        assertThat(resumen.duracionDias()).isEqualTo(8L);
        assertThat(resumen.actividadesFinalizadas()).isEqualTo(1);
        assertThat(resumen.estado()).isEqualTo(EstadoHito.EN_CURSO);
    }

    @Test
    void calculaAvanceDelProyectoPonderandoDuracionDeHitos() {
        Proyecto proyecto = Proyecto.builder().id(1L).porcentajeAvance(0D).build();
        Hito hitoCorto = Hito.builder().id(10L).estado(EstadoHito.PENDIENTE).build();
        Hito hitoLargo = Hito.builder().id(20L).estado(EstadoHito.PENDIENTE).build();
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));
        when(hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(1L)).thenReturn(List.of(hitoCorto, hitoLargo));
        when(actividadRepository.findByHitoIdOrderByFechaInicioAscIdAsc(10L)).thenReturn(List.of(
            actividad(1L, "2026-06-01", "2026-06-02", EstadoActividad.FINALIZADA)
        ));
        when(actividadRepository.findByHitoIdOrderByFechaInicioAscIdAsc(20L)).thenReturn(List.of(
            actividad(2L, "2026-06-03", "2026-06-08", EstadoActividad.PENDIENTE)
        ));

        service.recalcularProyecto(1L);

        assertThat(proyecto.getPorcentajeAvance()).isEqualTo(25D);
        assertThat(hitoCorto.getEstado()).isEqualTo(EstadoHito.FINALIZADO);
        assertThat(hitoLargo.getEstado()).isEqualTo(EstadoHito.PENDIENTE);
        verify(proyectoRepository).save(proyecto);
    }

    private Actividad actividad(Long id, String inicio, String fin, EstadoActividad estado) {
        return Actividad.builder()
            .id(id)
            .fechaInicio(LocalDate.parse(inicio))
            .fechaFin(LocalDate.parse(fin))
            .estado(estado)
            .build();
    }
}
