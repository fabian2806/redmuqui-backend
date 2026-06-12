package com.redmuqui.platform.reporte.service;

import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.reporte.dto.CoberturaTerritorialDTO;
import com.redmuqui.platform.reporte.dto.ConteoDTO;
import com.redmuqui.platform.reporte.dto.IndicadoresDTO;
import com.redmuqui.platform.reporte.dto.ProyectoRiesgoDTO;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.entity.TipoTerritorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private ProyectoRepository proyectoRepository;
    @Mock private ActividadRepository actividadRepository;
    @Mock private SubactividadRepository subactividadRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private HitoRepository hitoRepository;
    @Mock private TerritorioRepository territorioRepository;

    private ReporteService service() {
        return new ReporteService(
            proyectoRepository,
            actividadRepository,
            subactividadRepository,
            documentoRepository,
            hitoRepository,
            territorioRepository
        );
    }

    private Proyecto proyecto(long id, double avance, LocalDate fechaFin) {
        return Proyecto.builder()
            .id(id)
            .nombre("Proyecto " + id)
            .codigoInterno("PRY-" + id)
            .estado(EstadoProyecto.ACTIVO)
            .porcentajeAvance(avance)
            .fechaInicio(LocalDate.now().minusDays(100))
            .fechaFinEstimada(fechaFin)
            .build();
    }

    @Test
    void obtenerIndicadoresMapeaTodasLasCifras() {
        when(proyectoRepository.countByEstado(EstadoProyecto.ACTIVO)).thenReturn(14L);
        when(proyectoRepository.sumPresupuestoByEstado(EstadoProyecto.ACTIVO)).thenReturn(4_200_000.0);
        when(proyectoRepository.avgAvanceByEstado(EstadoProyecto.ACTIVO)).thenReturn(57.5);
        when(subactividadRepository.sumHombresInvolucrados()).thenReturn(1_620L);
        when(subactividadRepository.sumMujeresInvolucradas()).thenReturn(1_840L);
        when(documentoRepository.countByEstado(EstadoDocumento.PUBLICADO)).thenReturn(23L);
        when(documentoRepository.countByEstadoIn(anyList())).thenReturn(7L);
        // proyectosEnRiesgo() interno: sin proyectos activos en riesgo
        when(proyectoRepository.findByEstado(EstadoProyecto.ACTIVO)).thenReturn(List.of());
        when(hitoRepository.contarHitosVencidosPorProyecto(any())).thenReturn(List.of());

        IndicadoresDTO ind = service().obtenerIndicadores();

        assertThat(ind.proyectosActivos()).isEqualTo(14L);
        assertThat(ind.proyectosEnRiesgo()).isZero();
        assertThat(ind.presupuestoTotal()).isEqualTo(4_200_000.0);
        assertThat(ind.avancePromedio()).isEqualTo(57.5);
        assertThat(ind.beneficiariosHombres()).isEqualTo(1_620L);
        assertThat(ind.beneficiariosMujeres()).isEqualTo(1_840L);
        assertThat(ind.documentosPublicados()).isEqualTo(23L);
        assertThat(ind.documentosPendientes()).isEqualTo(7L);
    }

    @Test
    void actividadesPorEstadoDevuelveCuatroBucketsMutuamenteExcluyentes() {
        when(actividadRepository.countByEstado(EstadoActividad.FINALIZADA)).thenReturn(10L);
        when(actividadRepository.countVigentesByEstado(eq(EstadoActividad.EN_CURSO), any())).thenReturn(5L);
        when(actividadRepository.countVigentesByEstado(eq(EstadoActividad.PENDIENTE), any())).thenReturn(8L);
        when(actividadRepository.countVencidas(any())).thenReturn(3L);

        List<ConteoDTO> series = service().actividadesPorEstado();

        assertThat(series).extracting(ConteoDTO::etiqueta)
            .containsExactly("Finalizadas", "En curso", "Pendientes", "Vencidas");
        assertThat(series).extracting(ConteoDTO::cantidad)
            .containsExactly(10L, 5L, 8L, 3L);
    }

    @Test
    void proyectosPorMacroregionMapeaFilasAgrupadas() {
        when(proyectoRepository.contarPorMacroregion()).thenReturn(List.of(
            new Object[]{"Norte", 6L},
            new Object[]{"Sur", 4L},
            new Object[]{"Centro", 2L}
        ));

        List<ConteoDTO> series = service().proyectosPorMacroregion();

        assertThat(series).containsExactly(
            new ConteoDTO("Norte", 6L),
            new ConteoDTO("Sur", 4L),
            new ConteoDTO("Centro", 2L)
        );
    }

    @Test
    void proyectosEnRiesgoClasificaPorHitoVencidoYPorPlazoConBajoAvance() {
        Proyecto conHitoVencido = proyecto(1L, 90.0, LocalDate.now().plusDays(200)); // sano por plazo, pero hito vencido
        Proyecto porVencerBajoAvance = proyecto(2L, 30.0, LocalDate.now().plusDays(10)); // ≤30 días y <70%
        Proyecto sano = proyecto(3L, 95.0, LocalDate.now().plusDays(180)); // sin riesgo

        when(proyectoRepository.findByEstado(EstadoProyecto.ACTIVO))
            .thenReturn(List.of(conHitoVencido, porVencerBajoAvance, sano));
        when(hitoRepository.contarHitosVencidosPorProyecto(any()))
            .thenReturn(List.<Object[]>of(new Object[]{1L, 2L}));

        List<ProyectoRiesgoDTO> riesgo = service().proyectosEnRiesgo();

        // Ordena por hitos vencidos desc: primero el de 2 hitos vencidos
        assertThat(riesgo).extracting(ProyectoRiesgoDTO::id).containsExactly(1L, 2L);
        assertThat(riesgo).extracting(ProyectoRiesgoDTO::hitosVencidos).containsExactly(2L, 0L);
        assertThat(riesgo).noneMatch(dto -> dto.id() == 3L);
    }

    @Test
    void proyectoSinFechaFinYSinHitosVencidosNoEstaEnRiesgo() {
        Proyecto sinFecha = proyecto(9L, 10.0, null);
        when(proyectoRepository.findByEstado(EstadoProyecto.ACTIVO)).thenReturn(List.of(sinFecha));
        when(hitoRepository.contarHitosVencidosPorProyecto(any())).thenReturn(List.of());

        assertThat(service().proyectosEnRiesgo()).isEmpty();
    }

    @Test
    void coberturaTerritorialFusionaAgregadosYRellenaConCeros() {
        Territorio cajamarca = territorio(1L, "Cajamarca", "06");
        Territorio callao = territorio(2L, "Callao", "07"); // sin actividad → todo en cero
        when(territorioRepository.findByTipoOrderByNombreAsc(TipoTerritorio.DEPARTAMENTO))
            .thenReturn(List.of(cajamarca, callao));
        when(proyectoRepository.agregarPorTerritorio())
            .thenReturn(List.<Object[]>of(new Object[]{1L, 3L, 150_000.0}));
        when(proyectoRepository.contarInstitucionesPorTerritorio())
            .thenReturn(List.<Object[]>of(new Object[]{1L, 2L}));
        when(subactividadRepository.beneficiariosPorTerritorio())
            .thenReturn(List.<Object[]>of(new Object[]{1L, 500L}));

        List<CoberturaTerritorialDTO> cobertura = service().coberturaTerritorial(TipoTerritorio.DEPARTAMENTO);

        assertThat(cobertura).hasSize(2);

        CoberturaTerritorialDTO caj = cobertura.get(0);
        assertThat(caj.codigo()).isEqualTo("06");
        assertThat(caj.tipo()).isEqualTo("DEPARTAMENTO");
        assertThat(caj.proyectos()).isEqualTo(3L);
        assertThat(caj.presupuesto()).isEqualTo(150_000.0);
        assertThat(caj.beneficiarios()).isEqualTo(500L);
        assertThat(caj.instituciones()).isEqualTo(2L);

        CoberturaTerritorialDTO cal = cobertura.get(1);
        assertThat(cal.codigo()).isEqualTo("07");
        assertThat(cal.proyectos()).isZero();
        assertThat(cal.presupuesto()).isZero();
        assertThat(cal.beneficiarios()).isZero();
        assertThat(cal.instituciones()).isZero();
    }

    private Territorio territorio(long id, String nombre, String codigo) {
        Territorio t = new Territorio();
        t.setId(id);
        t.setNombre(nombre);
        t.setCodigo(codigo);
        t.setTipo(TipoTerritorio.DEPARTAMENTO);
        return t;
    }
}
