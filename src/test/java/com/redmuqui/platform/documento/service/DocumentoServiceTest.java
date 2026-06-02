package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.DocumentoCreateDTO;
import com.redmuqui.platform.documento.dto.DocumentoResponseDTO;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Pruebas del registro de documentos (RF-045/046/047) y su asociación a
 * proyecto / eje temático / territorios (RF-051/052/053). Mockito puro, sin BD.
 */
@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock private DocumentoRepository documentoRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private EjeTematicoRepository ejeTematicoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TerritorioRepository territorioRepository;

    @InjectMocks private DocumentoService service;

    private DocumentoCreateDTO dto(String tipo, EstadoDocumento estado,
                                   Long idProyecto, Long idEje,
                                   Long idRespElab, Long idRespVal, Set<Long> territorios) {
        return new DocumentoCreateDTO(
            "Informe de prueba", "Descripcion", tipo, estado,
            null, null, idProyecto, idEje, idRespElab, idRespVal, territorios);
    }

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
        when(territorioRepository.findAllById(any())).thenReturn(List.of()); // ninguno existe

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
}
