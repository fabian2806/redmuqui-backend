package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import com.redmuqui.platform.trazabilidad.repository.BitacoraRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitacoraServiceTest {

    @Mock private BitacoraRepository bitacoraRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private BitacoraService service;

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarEvento_persisteCamposRequeridos() {
        Usuario usuario = Usuario.builder().id(5L).email("tecnico@test.com").build();
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(bitacoraRepository.save(any(Bitacora.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarEvento("MODIFICACION", "PROYECTO", 10L, "Se modificó el proyecto con código PRY-01", 5L);

        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        verify(bitacoraRepository).save(captor.capture());
        Bitacora guardada = captor.getValue();

        assertThat(guardada.getTipoAccion()).isEqualTo("MODIFICACION");
        assertThat(guardada.getEntidadReferenciada()).isEqualTo("PROYECTO");
        assertThat(guardada.getIdEntidadRef()).isEqualTo(10L);
        assertThat(guardada.getDescripcion()).contains("PRY-01");
        assertThat(guardada.getFecha()).isNotNull();
        assertThat(guardada.getUsuario()).isEqualTo(usuario);
    }

    @Test
    void registrarEventoAutenticado_resuelveUsuarioDesdeSecurityContext() {
        configurarUsuarioAutenticado("coord@test.com");
        Usuario usuario = Usuario.builder().id(1L).email("coord@test.com").build();
        when(usuarioRepository.findByEmailIgnoreCase("coord@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(bitacoraRepository.save(any(Bitacora.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarEventoAutenticado("CREACION", "PROYECTO", 1L, "Se creó el proyecto con código PRY-001");

        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        verify(bitacoraRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getTipoAccion()).isEqualTo("CREACION");
    }

    @Test
    void resolverIdUsuarioAutenticado_fallaSinAutenticacion() {
        assertThatThrownBy(() -> service.resolverIdUsuarioAutenticado())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("usuario autenticado");
    }

    @Test
    void resolverIdUsuarioAutenticado_fallaSiEmailNoExiste() {
        configurarUsuarioAutenticado("desconocido@test.com");
        when(usuarioRepository.findByEmailIgnoreCase("desconocido@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolverIdUsuarioAutenticado())
            .isInstanceOf(ResourceNotFoundException.class);
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
