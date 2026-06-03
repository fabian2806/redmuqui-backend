package com.redmuqui.platform.trazabilidad.aspect;

import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.trazabilidad.service.BitacoraService;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitacoraAspectTest {

    @Mock private BitacoraService bitacoraService;

    @InjectMocks private BitacoraAspect aspect;

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void trasCrearProyecto_registraCreacionConCodigo() {
        configurarUsuarioAutenticado("admin@test.com");
        ProyectoResponseDTO proyecto = proyectoResponse(7L, "PRY-2026-007");

        aspect.trasCrearProyecto(null, proyecto);

        verify(bitacoraService).registrarEventoAutenticado(
            eq(BitacoraAspect.CREACION),
            eq(BitacoraAspect.ENTIDAD_PROYECTO),
            eq(7L),
            contains("PRY-2026-007")
        );
    }

    @Test
    void trasActualizarProyecto_registraModificacion() {
        configurarUsuarioAutenticado("admin@test.com");
        ProyectoResponseDTO proyecto = proyectoResponse(2L, "PRY-LEGACY");

        aspect.trasActualizarProyecto(null, proyecto);

        verify(bitacoraService).registrarEventoAutenticado(
            eq(BitacoraAspect.MODIFICACION),
            eq(BitacoraAspect.ENTIDAD_PROYECTO),
            eq(2L),
            contains("modificó")
        );
    }

    @Test
    void trasCrearHito_registraEntidadHito() {
        configurarUsuarioAutenticado("tecnico@test.com");
        HitoResponseDTO hito = new HitoResponseDTO(
                1L,
                "Nombre del Hito",
                "Descripción",
                LocalDate.now(),
                EstadoHito.PENDIENTE,
                9L,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
        );

        aspect.trasCrearHito(null, hito);

        verify(bitacoraService).registrarEventoAutenticado(
            eq(BitacoraAspect.CREACION),
            eq(BitacoraAspect.ENTIDAD_HITO),
            eq(1L),
            contains("Nombre del Hito")
        );
    }

    @Test
    void trasCrearActividad_registraEntidadActividad() {
        configurarUsuarioAutenticado("tecnico@test.com");
        ActividadResponseDTO actividad = new ActividadResponseDTO(
                20L,
                "Capacitación",
                "Desc",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                EstadoActividad.EN_CURSO,
                1,
                9L,
                Set.of(1L),
                java.util.List.of()
        );

        aspect.trasCrearActividad(null, actividad);

        verify(bitacoraService).registrarEventoAutenticado(
            eq(BitacoraAspect.CREACION),
            eq(BitacoraAspect.ENTIDAD_ACTIVIDAD),
            eq(20L),
            contains("Capacitación")
        );
    }

    @Test
    void registrarSeguro_noPropagaErrorDeBitacora() {
        configurarUsuarioAutenticado("admin@test.com");
        doThrow(new IllegalStateException("Sin usuario"))
            .when(bitacoraService)
            .registrarEventoAutenticado(
                eq(BitacoraAspect.CREACION),
                eq(BitacoraAspect.ENTIDAD_PROYECTO),
                eq(1L),
                contains("PRY-FAIL")
            );

        aspect.trasCrearProyecto(null, proyectoResponse(1L, "PRY-FAIL"));

        verify(bitacoraService).registrarEventoAutenticado(
            eq(BitacoraAspect.CREACION),
            eq(BitacoraAspect.ENTIDAD_PROYECTO),
            eq(1L),
            contains("PRY-FAIL")
        );
    }

    @Test
    void trasAgregarMiembro_registraModificacionDelProyecto() {
        configurarUsuarioAutenticado("coord@test.com");
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 11L, null });

        aspect.trasAgregarMiembro(joinPoint);

        verify(bitacoraService).registrarEventoAutenticado(
            eq(BitacoraAspect.MODIFICACION),
            eq(BitacoraAspect.ENTIDAD_PROYECTO),
            eq(11L),
            contains("miembro")
        );
    }

    private ProyectoResponseDTO proyectoResponse(Long id, String codigo) {
        return new ProyectoResponseDTO(
            id,
            "Proyecto demo",
            codigo,
            "Descripción",
            "Objetivo",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31),
            EstadoProyecto.ACTIVO,
            1,
            10.0,
            1000.0,
            null,
            null,
            Set.of(),
            null,
            null,
            null,
            Set.of(),Set.of()
        );
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
