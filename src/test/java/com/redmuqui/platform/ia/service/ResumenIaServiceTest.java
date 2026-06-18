package com.redmuqui.platform.ia.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.ia.client.LlmClient;
import com.redmuqui.platform.ia.dto.ResumenIaResponse;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumenIaServiceTest {

    @Mock private ProyectoRepository proyectoRepository;
    @Mock private ActividadRepository actividadRepository;
    @Mock private HitoRepository hitoRepository;
    @Mock private LlmClient llmClient;

    private ResumenIaService service() {
        return new ResumenIaService(proyectoRepository, actividadRepository, hitoRepository, llmClient);
    }

    private Proyecto proyectoBase() {
        return Proyecto.builder()
            .id(1L)
            .nombre("Agua Limpia")
            .codigoInterno("PR-001")
            .estado(EstadoProyecto.ACTIVO)
            .porcentajeAvance(45.0)
            .presupuesto(100_000.0)
            .fechaInicio(LocalDate.of(2026, 1, 1))
            .fechaFinEstimada(LocalDate.of(2030, 12, 31)) // lejos: no dispara riesgo por plazo
            .build();
    }

    @Test
    void sinIaConfiguradaDevuelvePlantillaTransparente() {
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyectoBase()));
        when(actividadRepository.findByProyectoId(1L)).thenReturn(List.of());
        when(hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(1L)).thenReturn(List.of());
        when(llmClient.estaConfigurado()).thenReturn(false);

        ResumenIaResponse r = service().generarResumen(1L);

        assertThat(r.generadoPorIa()).isFalse();
        assertThat(r.modelo()).isEqualTo("plantilla-local");
        assertThat(r.aviso()).isNotBlank();
        assertThat(r.resumen()).contains("Agua Limpia").contains("45%");
        verify(llmClient, never()).generar(anyString(), anyString());
    }

    @Test
    void conIaConfiguradaUsaElTextoDelModelo() {
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyectoBase()));
        when(actividadRepository.findByProyectoId(1L)).thenReturn(List.of());
        when(hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(1L)).thenReturn(List.of());
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.modelo()).thenReturn("gemini-2.0-flash");
        when(llmClient.generar(anyString(), anyString())).thenReturn("Resumen ejecutivo redactado por IA.");

        ResumenIaResponse r = service().generarResumen(1L);

        assertThat(r.generadoPorIa()).isTrue();
        assertThat(r.modelo()).isEqualTo("gemini-2.0-flash");
        assertThat(r.resumen()).isEqualTo("Resumen ejecutivo redactado por IA.");
        assertThat(r.aviso()).isNull();
    }

    @Test
    void siLaIaFallaCaeAlaPlantillaSinPropagarError() {
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyectoBase()));
        when(actividadRepository.findByProyectoId(1L)).thenReturn(List.of());
        when(hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(1L)).thenReturn(List.of());
        when(llmClient.estaConfigurado()).thenReturn(true);
        when(llmClient.generar(anyString(), anyString())).thenThrow(new RuntimeException("503 upstream"));

        ResumenIaResponse r = service().generarResumen(1L);

        assertThat(r.generadoPorIa()).isFalse();
        assertThat(r.modelo()).isEqualTo("plantilla-local");
        assertThat(r.resumen()).contains("Agua Limpia");
        assertThat(r.aviso()).contains("IA");
    }

    @Test
    void proyectoInexistenteLanza404() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().generarResumen(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void agregaBeneficiariosYDetectaRiesgoPorHitoVencido() {
        Subactividad s1 = Subactividad.builder()
            .hombresInvolucrados(10).mujeresInvolucradas(15).presupuesto(5_000.0).build();
        Subactividad s2 = Subactividad.builder()
            .hombresInvolucrados(4).mujeresInvolucradas(6).presupuesto(2_000.0).build();
        Actividad actividad = Actividad.builder()
            .estado(EstadoActividad.FINALIZADA)
            .subactividades(new java.util.ArrayList<>(List.of(s1, s2)))
            .build();
        Hito hitoVencido = Hito.builder()
            .nombre("Diagnóstico")
            .fechaClave(LocalDate.now().minusDays(5))
            .estado(EstadoHito.PENDIENTE)
            .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyectoBase()));
        when(actividadRepository.findByProyectoId(1L)).thenReturn(List.of(actividad));
        when(hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(1L)).thenReturn(List.of(hitoVencido));
        when(llmClient.estaConfigurado()).thenReturn(false);

        ResumenIaResponse r = service().generarResumen(1L);

        assertThat(r.resumen())
            .contains("35 beneficiarios")
            .contains("14 hombres y 21 mujeres")
            .contains("alertas de riesgo");
    }
}
