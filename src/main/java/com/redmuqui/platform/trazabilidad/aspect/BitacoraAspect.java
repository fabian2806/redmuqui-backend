package com.redmuqui.platform.trazabilidad.aspect;

import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.trazabilidad.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Registro automático de eventos de negocio (HU020, RF-062, RF-063).
 * Solo intercepta operaciones de persistencia sobre Proyecto y entidades directamente relacionadas.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class BitacoraAspect {

    static final String CREACION = "CREACION";
    static final String MODIFICACION = "MODIFICACION";
    static final String ENTIDAD_PROYECTO = "PROYECTO";
    static final String ENTIDAD_HITO = "HITO";
    static final String ENTIDAD_ACTIVIDAD = "ACTIVIDAD";

    private final BitacoraService bitacoraService;

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.proyecto.service.ProyectoService.crear(..))",
        returning = "result"
    )
    public void trasCrearProyecto(JoinPoint joinPoint, ProyectoResponseDTO result) {
        registrarSeguro(
            CREACION,
            ENTIDAD_PROYECTO,
            result.id(),
            "Se creó el proyecto con código " + result.codigoInterno()
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.proyecto.service.ProyectoService.actualizar(..))",
        returning = "result"
    )
    public void trasActualizarProyecto(JoinPoint joinPoint, ProyectoResponseDTO result) {
        registrarSeguro(
            MODIFICACION,
            ENTIDAD_PROYECTO,
            result.id(),
            "Se modificó el proyecto con código " + result.codigoInterno()
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.proyecto.service.ProyectoService.agregarMiembro(..))"
    )
    public void trasAgregarMiembro(JoinPoint joinPoint) {
        Long idProyecto = (Long) joinPoint.getArgs()[0];
        registrarSeguro(
            MODIFICACION,
            ENTIDAD_PROYECTO,
            idProyecto,
            "Se agregó un miembro al equipo del proyecto con ID " + idProyecto
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.proyecto.service.ProyectoService.asociarInstituciones(..))"
    )
    public void trasAsociarInstituciones(JoinPoint joinPoint) {
        Long idProyecto = (Long) joinPoint.getArgs()[0];
        registrarSeguro(
            MODIFICACION,
            ENTIDAD_PROYECTO,
            idProyecto,
            "Se asociaron instituciones al proyecto con ID " + idProyecto
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.proyecto.service.ProyectoService.asociarTerritorios(..))"
    )
    public void trasAsociarTerritorios(JoinPoint joinPoint) {
        Long idProyecto = (Long) joinPoint.getArgs()[0];
        registrarSeguro(
            MODIFICACION,
            ENTIDAD_PROYECTO,
            idProyecto,
            "Se asociaron territorios al proyecto con ID " + idProyecto
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.actividad.service.HitoService.crear(..))",
        returning = "result"
    )
    public void trasCrearHito(JoinPoint joinPoint, HitoResponseDTO result) {
        registrarSeguro(
            CREACION,
            ENTIDAD_HITO,
            result.id(),
            "Se creó el hito \"" + result.nombre() + "\" del proyecto con ID " + result.idProyecto()
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.actividad.service.HitoService.cambiarEstado(..))",
        returning = "result"
    )
    public void trasCambiarEstadoHito(JoinPoint joinPoint, HitoResponseDTO result) {
        registrarSeguro(
            MODIFICACION,
            ENTIDAD_HITO,
            result.id(),
            "Se modificó el estado del hito \"" + result.nombre() + "\" a " + result.estado()
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.actividad.service.ActividadService.crear(..))",
        returning = "result"
    )
    public void trasCrearActividad(JoinPoint joinPoint, ActividadResponseDTO result) {
        registrarSeguro(
            CREACION,
            ENTIDAD_ACTIVIDAD,
            result.id(),
            "Se creó la actividad \"" + result.nombre() + "\" del proyecto con ID " + result.idProyecto()
        );
    }

    @AfterReturning(
        pointcut = "execution(* com.redmuqui.platform.actividad.service.ActividadService.cambiarEstado(..))",
        returning = "result"
    )
    public void trasCambiarEstadoActividad(JoinPoint joinPoint, ActividadResponseDTO result) {
        registrarSeguro(
            MODIFICACION,
            ENTIDAD_ACTIVIDAD,
            result.id(),
            "Se modificó el estado de la actividad \"" + result.nombre() + "\" a " + result.estado()
        );
    }

    private void registrarSeguro(String tipoAccion, String entidad, Long idEntidad, String descripcion) {
        try {
            bitacoraService.registrarEventoAutenticado(tipoAccion, entidad, idEntidad, descripcion);
        } catch (Exception ex) {
            log.warn("No se pudo registrar evento en bitácora [{} {}]: {}", entidad, idEntidad, ex.getMessage());
        }
    }
}
