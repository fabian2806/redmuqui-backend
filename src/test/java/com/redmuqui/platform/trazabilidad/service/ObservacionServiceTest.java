package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.trazabilidad.dto.ObservacionRequestDTO;
import com.redmuqui.platform.trazabilidad.dto.ObservacionResponseDTO;
import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;
import com.redmuqui.platform.trazabilidad.entity.Observacion;
import com.redmuqui.platform.trazabilidad.repository.ObservacionRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservacionServiceTest {

    @Mock private ObservacionRepository observacionRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private ObservacionService service;

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crear_asignaEstadoPendienteFechaYUsuarioDelContexto() {
        configurarUsuarioAutenticado("tecnico@test.com");
        Usuario usuario = Usuario.builder()
            .id(8L)
            .email("tecnico@test.com")
            .nombres("Ana")
            .apellidos("García")
            .build();
        ObservacionRequestDTO request = new ObservacionRequestDTO(
            "Falta documentación de avance",
            "PROYECTO",
            42L
        );

        when(usuarioRepository.findByEmailIgnoreCase("tecnico@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(8L)).thenReturn(Optional.of(usuario));
        when(observacionRepository.save(any(Observacion.class))).thenAnswer(inv -> {
            Observacion o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        ObservacionResponseDTO response = service.crear(request);

        ArgumentCaptor<Observacion> captor = ArgumentCaptor.forClass(Observacion.class);
        verify(observacionRepository).save(captor.capture());
        Observacion guardada = captor.getValue();

        assertThat(guardada.getEstado()).isEqualTo(EstadoObservacion.PENDIENTE);
        assertThat(guardada.getFecha()).isNotNull();
        assertThat(guardada.getDescripcion()).isEqualTo("Falta documentación de avance");
        assertThat(guardada.getEntidadReferenciada()).isEqualTo("PROYECTO");
        assertThat(guardada.getIdEntidadRef()).isEqualTo(42L);
        assertThat(guardada.getUsuario()).isEqualTo(usuario);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.estado()).isEqualTo(EstadoObservacion.PENDIENTE);
        assertThat(response.idEntidadReferenciada()).isEqualTo(42L);
        assertThat(response.emailUsuario()).isEqualTo("tecnico@test.com");
        assertThat(response.nombreUsuario()).isEqualTo("Ana García");
    }

    @Test
    void listarPorEntidad_devuelvePaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 5);
        Usuario usuario = Usuario.builder()
            .id(2L)
            .email("coord@test.com")
            .nombres("Luis")
            .apellidos("Pérez")
            .build();
        Observacion observacion = Observacion.builder()
            .id(10L)
            .descripcion("Incidencia reportada")
            .fecha(LocalDateTime.of(2026, 6, 1, 10, 0))
            .estado(EstadoObservacion.PENDIENTE)
            .entidadReferenciada("DOCUMENTO")
            .idEntidadRef(99L)
            .usuario(usuario)
            .build();
        Page<Observacion> page = new PageImpl<>(List.of(observacion), pageable, 1);

        when(observacionRepository.findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc(
            "DOCUMENTO", 99L, pageable)).thenReturn(page);

        Page<ObservacionResponseDTO> result = service.listarPorEntidad("DOCUMENTO", 99L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        ObservacionResponseDTO dto = result.getContent().getFirst();
        assertThat(dto.descripcion()).isEqualTo("Incidencia reportada");
        assertThat(dto.entidadReferenciada()).isEqualTo("DOCUMENTO");
        assertThat(dto.idEntidadReferenciada()).isEqualTo(99L);
        assertThat(dto.emailUsuario()).isEqualTo("coord@test.com");

        verify(observacionRepository).findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc(
            eq("DOCUMENTO"), eq(99L), eq(pageable));
    }

    private void configurarUsuarioAutenticado(String email) {
        UserDetails userDetails = User.builder()
            .username(email)
            .password("secret")
            .authorities(List.of())
            .build();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
