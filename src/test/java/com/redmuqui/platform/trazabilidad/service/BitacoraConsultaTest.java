package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.trazabilidad.dto.BitacoraConsultaDTO;
import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import com.redmuqui.platform.trazabilidad.repository.BitacoraRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitacoraConsultaTest {

    @Mock private BitacoraRepository bitacoraRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private BitacoraService service;

    @Test
    void consultarGeneral_usaRepositorioPaginadoYOrdenPorFecha() {
        Pageable pageable = PageRequest.of(1, 5);
        Bitacora evento = eventoConUsuario("CREACION", "Proyecto creado", usuarioConNombre());
        when(bitacoraRepository.findAllByOrderByFechaDesc(pageable))
            .thenReturn(new PageImpl<>(List.of(evento), pageable, 6));

        Page<BitacoraConsultaDTO> resultado = service.consultarGeneral(pageable);

        verify(bitacoraRepository).findAllByOrderByFechaDesc(eq(pageable));
        assertThat(resultado.getNumber()).isEqualTo(1);
        assertThat(resultado.getSize()).isEqualTo(5);
        assertThat(resultado.getTotalElements()).isEqualTo(6);
        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    void consultarHistorialEntidad_filtraPorEntidadPolimorficaPaginado() {
        Pageable pageable = PageRequest.of(0, 10);
        Bitacora evento = eventoConUsuario("MODIFICACION", "Cambio de estado", usuarioConNombre());
        evento.setEntidadReferenciada("PROYECTO");
        evento.setIdEntidadRef(42L);
        when(bitacoraRepository.findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc("PROYECTO", 42L, pageable))
            .thenReturn(new PageImpl<>(List.of(evento), pageable, 1));

        Page<BitacoraConsultaDTO> resultado = service.consultarHistorialEntidad("PROYECTO", 42L, pageable);

        verify(bitacoraRepository)
            .findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc("PROYECTO", 42L, pageable);
        assertThat(resultado.getTotalElements()).isOne();
        assertThat(resultado.getContent().getFirst().tipoAccion()).isEqualTo("MODIFICACION");
    }

    @Test
    void toConsultaDTO_mapeaLosCuatroCampos() {
        LocalDateTime fecha = LocalDateTime.of(2026, 6, 2, 14, 30);
        Usuario usuario = usuarioConNombre();
        Bitacora evento = eventoConUsuario("CREACION", "Se creó el proyecto PRY-001", usuario);
        evento.setFecha(fecha);

        when(bitacoraRepository.findAllByOrderByFechaDesc(PageRequest.of(0, 1)))
            .thenReturn(new PageImpl<>(List.of(evento)));

        BitacoraConsultaDTO dto = service.consultarGeneral(PageRequest.of(0, 1)).getContent().getFirst();

        assertThat(dto.nombre()).isEqualTo("Ana García");
        assertThat(dto.descripcion()).isEqualTo("Se creó el proyecto PRY-001");
        assertThat(dto.tipoAccion()).isEqualTo("CREACION");
        assertThat(dto.fecha()).isEqualTo(fecha);
    }

    @Test
    void toConsultaDTO_usaEmailCuandoNoHayNombreCompleto() {
        Usuario usuario = Usuario.builder()
            .id(2L)
            .nombres(null)
            .apellidos(null)
            .email("solo.email@test.com")
            .build();
        Bitacora evento = eventoConUsuario("MODIFICACION", "Actualización", usuario);
        when(bitacoraRepository.findAllByOrderByFechaDesc(PageRequest.of(0, 1)))
            .thenReturn(new PageImpl<>(List.of(evento)));

        BitacoraConsultaDTO dto = service.consultarGeneral(PageRequest.of(0, 1)).getContent().getFirst();

        assertThat(dto.nombre()).isEqualTo("solo.email@test.com");
    }

    @Test
    void consultarHistorialEntidad_paginaVaciaSinEventos() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bitacoraRepository.findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc("PROYECTO", 99L, pageable))
            .thenReturn(Page.empty(pageable));

        Page<BitacoraConsultaDTO> resultado = service.consultarHistorialEntidad("PROYECTO", 99L, pageable);

        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
        assertThat(resultado.isFirst()).isTrue();
    }

    private static Usuario usuarioConNombre() {
        return Usuario.builder()
            .id(1L)
            .nombres("Ana")
            .apellidos("García")
            .email("ana@test.com")
            .build();
    }

    private static Bitacora eventoConUsuario(String tipoAccion, String descripcion, Usuario usuario) {
        return Bitacora.builder()
            .id(10L)
            .tipoAccion(tipoAccion)
            .descripcion(descripcion)
            .fecha(LocalDateTime.now())
            .usuario(usuario)
            .build();
    }
}
