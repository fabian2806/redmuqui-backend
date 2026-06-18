package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.Fase;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacionCronogramaServiceTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private SubactividadRepository subactividadRepository;

    private ValidacionCronogramaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ValidacionCronogramaService(actividadRepository, subactividadRepository);
    }

    @Test
    void aceptaFaseDentroDelProyecto() {
        Proyecto proyecto = Proyecto.builder()
            .fechaInicio(LocalDate.of(2026, 6, 1))
            .fechaFinEstimada(LocalDate.of(2026, 6, 30))
            .build();

        assertDoesNotThrow(() -> service.validarFaseEnProyecto(
            proyecto,
            LocalDate.of(2026, 6, 5),
            LocalDate.of(2026, 6, 25)
        ));
    }

    @Test
    void rechazaFaseFueraDelProyecto() {
        Proyecto proyecto = Proyecto.builder()
            .fechaInicio(LocalDate.of(2026, 6, 1))
            .fechaFinEstimada(LocalDate.of(2026, 6, 30))
            .build();

        assertThrows(BusinessException.class, () -> service.validarFaseEnProyecto(
            proyecto,
            LocalDate.of(2026, 5, 31),
            LocalDate.of(2026, 6, 25)
        ));
    }

    @Test
    void rechazaActividadFueraDeLaFase() {
        Fase fase = Fase.builder()
            .fechaInicioPlanificada(LocalDate.of(2026, 6, 10))
            .fechaFinPlanificada(LocalDate.of(2026, 6, 20))
            .build();

        assertThrows(BusinessException.class, () -> service.validarActividadEnFase(
            fase,
            LocalDate.of(2026, 6, 9),
            LocalDate.of(2026, 6, 15)
        ));
    }

    @Test
    void rechazaSubactividadFueraDeLaActividad() {
        Actividad actividad = Actividad.builder()
            .fechaInicioPlanificada(LocalDate.of(2026, 6, 11))
            .fechaFinPlanificada(LocalDate.of(2026, 6, 14))
            .build();

        assertThrows(BusinessException.class, () -> service.validarSubactividadEnActividad(
            actividad,
            LocalDate.of(2026, 6, 11),
            LocalDate.of(2026, 6, 15)
        ));
    }

    @Test
    void rechazaInicioRealAnteriorAlInicioPlanificado() {
        assertThrows(BusinessException.class, () -> service.validarFechaReal(
            LocalDate.of(2026, 6, 16),
            LocalDate.of(2026, 6, 14),
            LocalDate.of(2026, 6, 15),
            "la subactividad"
        ));
    }

    @Test
    void aceptaEjecucionRealDentroDelRangoLogico() {
        assertDoesNotThrow(() -> service.validarFechaReal(
            LocalDate.of(2026, 6, 16),
            LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 6, 15),
            "la subactividad"
        ));
    }
}
