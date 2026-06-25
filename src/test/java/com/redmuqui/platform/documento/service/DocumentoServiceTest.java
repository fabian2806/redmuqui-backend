package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import com.redmuqui.platform.documento.dto.DocumentoCreateDTO;
import com.redmuqui.platform.documento.dto.DocumentoResponseDTO;
import com.redmuqui.platform.documento.dto.DocumentoUpdateDTO;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.documento.repository.ArchivoRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.actividad.service.AvanceProyectoService;
import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoSubactividad;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.documento.entity.TipoVinculoDocumento;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del registro (RF-045/046/047), edición (RF-048/054/055) y flujo de
 * estados (RF-056) de documentos. Mockito puro, sin BD.
 */
@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock private DocumentoRepository documentoRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private EjeTematicoRepository ejeTematicoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TerritorioRepository territorioRepository;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private DocumentoVersionService documentoVersionService;
    @Mock private SubactividadRepository subactividadRepository;
    @Mock private ArchivoRepository archivoRepository;
    @Mock private AvanceProyectoService avanceProyectoService;

    @InjectMocks private DocumentoService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private DocumentoCreateDTO dto(String tipo, EstadoDocumento estado,
                                   Long idProyecto, Long idEje,
                                   Long idRespElab, Long idRespVal, Set<Long> territorios) {
        return new DocumentoCreateDTO(
            "Informe de prueba", "Descripcion", tipo, estado,
            null, null, idProyecto, null, null, idEje, idRespElab, idRespVal, territorios);
    }

    private DocumentoUpdateDTO updateDto(String tipo, EstadoDocumento estado,
                                         Long idRespElab, Long idRespVal) {
        return new DocumentoUpdateDTO(
            "Informe actualizado", "Nueva descripción", tipo, estado,
            null, null, LocalDate.of(2025, 6, 1),
            null, null, null, null, idRespElab, idRespVal, null);
    }

    /** Instala un Authentication con las authorities indicadas en el SecurityContext. */
    private void autenticarCon(String... authorities) {
        var granted = java.util.Arrays.stream(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList();
        var auth = new TestingAuthenticationToken("user", "pass", granted);
        auth.setAuthenticated(true);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    /** Crea un Documento stub con el estado indicado y lo registra en el repo mock. */
    private Documento documentoStubEnEstado(EstadoDocumento estado) {
        Usuario validador = mock(Usuario.class);
        Documento doc = Documento.builder()
            .id(1L)
            .titulo("Stub")
            .tipo("Informe")
            .estado(estado)
            .fechaCarga(LocalDate.now())
            .version(1.0)
            .respValidacion(validador)
            .build();
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        org.mockito.Mockito.lenient()
            .when(archivoRepository.existsByDocumentoId(1L))
            .thenReturn(true);
        return doc;
    }

    // ─── Tests RF-045/046/047/051/052/053 (creación) ──────────────────────────

    @Test
    void crearPersisteCamposYAsociaciones() {
        Usuario respElab = mock(Usuario.class);
        when(respElab.getId()).thenReturn(10L);
        Usuario respVal = mock(Usuario.class);
        when(respVal.getId()).thenReturn(11L);
        Proyecto proyecto = mock(Proyecto.class);
        when(proyecto.getId()).thenReturn(5L);
        EjeTematico eje = mock(EjeTematico.class);
        when(eje.getId()).thenReturn(3L);
        Territorio t7 = mock(Territorio.class);
        when(t7.getId()).thenReturn(7L);
        Territorio t8 = mock(Territorio.class);
        when(t8.getId()).thenReturn(8L);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(respElab));
        when(usuarioRepository.findById(11L)).thenReturn(Optional.of(respVal));
        when(authenticatedUserService.obtenerUsuario()).thenReturn(respElab);
        when(proyectoRepository.findById(5L)).thenReturn(Optional.of(proyecto));
        when(ejeTematicoRepository.findById(3L)).thenReturn(Optional.of(eje));
        when(territorioRepository.findAllById(any())).thenReturn(List.of(t7, t8));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentoResponseDTO result = service.crear(
            dto("Informe", null, 5L, 3L, 10L, 11L, Set.of(7L, 8L)));

        assertThat(result.tipo()).isEqualTo("Informe");
        assertThat(result.estado()).isEqualTo(EstadoDocumento.BORRADOR);
        assertThat(result.idProyecto()).isEqualTo(5L);
        assertThat(result.idEjeTematico()).isEqualTo(3L);
        assertThat(result.idRespElaboracion()).isEqualTo(10L);
        assertThat(result.idRespValidacion()).isEqualTo(11L);
        assertThat(result.idTerritorios()).containsExactlyInAnyOrder(7L, 8L);
        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    void crearUsaBorradorPorDefectoCuandoEstadoEsNull() {
        Usuario respElab = mock(Usuario.class);
        when(respElab.getId()).thenReturn(10L);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(respElab));
        when(authenticatedUserService.obtenerUsuario()).thenReturn(respElab);
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentoResponseDTO result = service.crear(
            dto("Manual", null, null, null, 10L, null, null));

        assertThat(result.estado()).isEqualTo(EstadoDocumento.BORRADOR);
    }

    @Test
    void crearRechazaTipoNoPermitido() {
        assertThatThrownBy(() -> service.crear(
            dto("Oficio", null, null, null, 10L, null, null)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("tipo");
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void crearRechazaTipoNulo() {
        assertThatThrownBy(() -> service.crear(
            dto(null, null, null, null, 10L, null, null)))
            .isInstanceOf(BusinessException.class);
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void crearRechazaTerritorioInexistente() {
        Usuario respElab = mock(Usuario.class);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(respElab));
        when(territorioRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.crear(
            dto("Informe", null, null, null, 10L, null, Set.of(999L))))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Territorio");
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void crearRechazaResponsableElaboracionInexistente() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(
            dto("Informe", null, null, null, 404L, null, null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuario");
        verify(documentoRepository, never()).save(any());
    }

    // ─── Tests RF-048/054/055 (actualización) ─────────────────────────────────

    @Test
    void actualizarPersisteCambiosEnCampos() {
        autenticarCon("DOCUMENTOS_UPDATE");
        Documento doc = documentoStubEnEstado(EstadoDocumento.BORRADOR);
        Usuario respElab = mock(Usuario.class);
        when(respElab.getId()).thenReturn(10L);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(respElab));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentoResponseDTO result = service.actualizar(1L,
            updateDto("Manual", EstadoDocumento.BORRADOR, 10L, null));

        assertThat(result.titulo()).isEqualTo("Informe actualizado");
        assertThat(result.tipo()).isEqualTo("Manual");
        assertThat(result.fechaCarga()).isEqualTo(LocalDate.of(2025, 6, 1));
        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    void actualizarRechazaTipoNoPermitido() {
        autenticarCon("DOCUMENTOS_UPDATE");
        documentoStubEnEstado(EstadoDocumento.BORRADOR);

        assertThatThrownBy(() -> service.actualizar(1L,
            updateDto("Oficio", EstadoDocumento.BORRADOR, 10L, null)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("tipo");
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void actualizarRechazaResponsableElaboracionInexistente() {
        autenticarCon("DOCUMENTOS_UPDATE");
        documentoStubEnEstado(EstadoDocumento.BORRADOR);
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(1L,
            updateDto("Informe", EstadoDocumento.BORRADOR, 999L, null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Usuario");
        verify(documentoRepository, never()).save(any());
    }

    // ─── Tests RF-056 (flujo de estados) ──────────────────────────────────────

    @Test
    void eliminarBorraDocumentoExistente() {
        Documento doc = documentoStubEnEstado(EstadoDocumento.BORRADOR);

        service.eliminar(1L);

        verify(documentoRepository).delete(doc);
    }

    @Test
    void eliminarRechazaDocumentoInexistente() {
        when(documentoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(404L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Documento");
        verify(documentoRepository, never()).delete(any(Documento.class));
    }

    @Test
    void cambiarEstadoBorradorAEnRevisionValido() {
        autenticarCon("DOCUMENTOS_UPDATE");
        documentoStubEnEstado(EstadoDocumento.BORRADOR);

        DocumentoResponseDTO result = service.cambiarEstado(1L, EstadoDocumento.EN_REVISION);

        assertThat(result.estado()).isEqualTo(EstadoDocumento.EN_REVISION);
    }

    @Test
    void cambiarEstadoEnRevisionAPublicadoValido() {
        autenticarCon("DOCUMENTOS_VALIDATE");
        documentoStubEnEstado(EstadoDocumento.EN_REVISION);

        DocumentoResponseDTO result = service.cambiarEstado(1L, EstadoDocumento.PUBLICADO);

        assertThat(result.estado()).isEqualTo(EstadoDocumento.PUBLICADO);
    }

    @Test
    void cambiarEstadoBorradorAPublicadoRechazado() {
        autenticarCon("DOCUMENTOS_UPDATE");
        documentoStubEnEstado(EstadoDocumento.BORRADOR);

        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoDocumento.PUBLICADO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("BORRADOR → PUBLICADO");
    }

    @Test
    void cambiarEstadoTransicionInvalidaRechazada() {
        autenticarCon("DOCUMENTOS_VALIDATE");
        documentoStubEnEstado(EstadoDocumento.PUBLICADO);

        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoDocumento.BORRADOR))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("no permitida");
    }

    @Test
    void publicarEntregableFinalCompletaSubactividadYRecalculaJerarquia() {
        autenticarCon("DOCUMENTOS_VALIDATE");
        Actividad actividad = mock(Actividad.class);
        Proyecto proyecto = mock(Proyecto.class);
        when(actividad.getId()).thenReturn(20L);
        when(proyecto.getId()).thenReturn(5L);

        Subactividad subactividad = Subactividad.builder()
            .id(30L)
            .nombre("Preparar informe")
            .actividad(actividad)
            .costoReal(1200D)
            .fechaInicioPlanificada(LocalDate.now().minusDays(2))
            .estado(EstadoSubactividad.EN_CURSO)
            .porcentajeAvance(50)
            .build();
        Documento documento = Documento.builder()
            .id(1L)
            .titulo("Entregable final")
            .tipo("Informe")
            .estado(EstadoDocumento.EN_REVISION)
            .fechaCarga(LocalDate.now())
            .version(2D)
            .proyecto(proyecto)
            .subactividad(subactividad)
            .tipoVinculo(TipoVinculoDocumento.ENTREGABLE_FINAL)
            .respValidacion(mock(Usuario.class))
            .build();
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(archivoRepository.existsByDocumentoId(1L)).thenReturn(true);

        DocumentoResponseDTO result = service.cambiarEstado(1L, EstadoDocumento.PUBLICADO);

        assertThat(result.estado()).isEqualTo(EstadoDocumento.PUBLICADO);
        assertThat(result.idSubactividad()).isEqualTo(30L);
        assertThat(subactividad.getEstado()).isEqualTo(EstadoSubactividad.FINALIZADA);
        assertThat(subactividad.getPorcentajeAvance()).isEqualTo(100);
        assertThat(subactividad.getFechaFinReal()).isEqualTo(LocalDate.now());
        verify(avanceProyectoService).recalcularActividad(20L);
    }

    @Test
    void devolverEntregableARevisionReabreSubactividad() {
        autenticarCon("DOCUMENTOS_VALIDATE");
        Actividad actividad = mock(Actividad.class);
        Proyecto proyecto = mock(Proyecto.class);
        when(actividad.getId()).thenReturn(20L);
        when(proyecto.getId()).thenReturn(5L);

        Subactividad subactividad = Subactividad.builder()
            .id(30L)
            .nombre("Preparar informe")
            .actividad(actividad)
            .costoReal(1200D)
            .fechaInicioPlanificada(LocalDate.now().minusDays(2))
            .fechaFinReal(LocalDate.now())
            .estado(EstadoSubactividad.FINALIZADA)
            .porcentajeAvance(100)
            .build();
        Documento documento = Documento.builder()
            .id(1L)
            .titulo("Entregable final")
            .tipo("Informe")
            .estado(EstadoDocumento.PUBLICADO)
            .fechaCarga(LocalDate.now())
            .version(3D)
            .proyecto(proyecto)
            .subactividad(subactividad)
            .tipoVinculo(TipoVinculoDocumento.ENTREGABLE_FINAL)
            .respValidacion(mock(Usuario.class))
            .build();
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));

        service.cambiarEstado(1L, EstadoDocumento.EN_REVISION);

        assertThat(subactividad.getEstado()).isEqualTo(EstadoSubactividad.EN_CURSO);
        assertThat(subactividad.getPorcentajeAvance()).isEqualTo(50);
        assertThat(subactividad.getFechaFinReal()).isNull();
        verify(avanceProyectoService).recalcularActividad(20L);
    }

    @Test
    void publicarEntregableSinCostoRealEsRechazado() {
        autenticarCon("DOCUMENTOS_VALIDATE");
        Actividad actividad = mock(Actividad.class);
        Subactividad subactividad = Subactividad.builder()
            .id(30L)
            .nombre("Preparar informe")
            .actividad(actividad)
            .fechaInicioPlanificada(LocalDate.now().minusDays(1))
            .estado(EstadoSubactividad.EN_CURSO)
            .build();
        Documento documento = Documento.builder()
            .id(1L)
            .titulo("Entregable final")
            .tipo("Informe")
            .estado(EstadoDocumento.EN_REVISION)
            .fechaCarga(LocalDate.now())
            .version(2D)
            .subactividad(subactividad)
            .tipoVinculo(TipoVinculoDocumento.ENTREGABLE_FINAL)
            .respValidacion(mock(Usuario.class))
            .build();
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(archivoRepository.existsByDocumentoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoDocumento.PUBLICADO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("costo real");
        verify(avanceProyectoService, never()).recalcularActividad(any());
    }
}
