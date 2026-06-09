package com.redmuqui.platform.trazabilidad.aspect;

import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.macroregion.entity.Macroregion;
import com.redmuqui.platform.macroregion.repository.MacroregionRepository;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.trazabilidad.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
    private final ProyectoRepository proyectoRepository;
    private final MacroregionRepository  macroregionRepository;
    private final TerritorioRepository  territorioRepository;

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

//    @AfterReturning(
//        pointcut = "execution(* com.redmuqui.platform.proyecto.service.ProyectoService.asociarInstituciones(..))"
//    )
//    public void trasAsociarInstituciones(JoinPoint joinPoint) {
//        Long idProyecto = (Long) joinPoint.getArgs()[0];
//        registrarSeguro(
//            MODIFICACION,
//            ENTIDAD_PROYECTO,
//            idProyecto,
//            "Se asociaron instituciones al proyecto con ID " + idProyecto
//        );
//    }

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

//    @AfterReturning(
//        pointcut = "execution(* com.redmuqui.platform.actividad.service.ActividadService.cambiarEstado(..))",
//        returning = "result"
//    )
//    public void trasCambiarEstadoActividad(JoinPoint joinPoint, ActividadResponseDTO result) {
//        registrarSeguro(
//            MODIFICACION,
//            ENTIDAD_ACTIVIDAD,
//            result.id(),
//            "Se modificó el estado de la actividad \"" + result.nombre() + "\" a " + result.estado()
//        );
//    }
//
    private void registrarSeguro(String tipoAccion, String entidad, Long idEntidad, String descripcion) {
        try {
            bitacoraService.registrarEventoAutenticado(tipoAccion, entidad, idEntidad, descripcion);
        } catch (Exception ex) {
            log.warn("No se pudo registrar evento en bitácora [{} {}]: {}", entidad, idEntidad, ex.getMessage());
        }
    }


    @Around("execution(* com.redmuqui.platform.proyecto.service.ProyectoService.actualizar(..))")
    public Object auditarActualizacionProyecto(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long idProyecto = (Long) args[0];
        ProyectoUpdateDTO dto = (ProyectoUpdateDTO) args[1]; // El DTO con los datos nuevos

        // 1. VARIABLES PARA ALMACENAR EL ESTADO ANTERIOR
        String oldNombre = null, oldCodigo = null, oldDesc = null, oldObj = null;
        String oldInicio = null, oldFin = null, oldEstado = null;
        String oldPrioridad = null, oldAvance = null, oldPresupuesto = null;

        // Variables para campos complejos (Guardaremos sus IDs como String para comparar)
        String oldEjeTematicoID = "Ninguno";String oldEjeTematicoName = "Ninguno";
        String oldResponsableID = "Ninguno";String oldResponsableName = "Ninguno";
        String oldMacroregionesID = "Ninguna";String oldMacroregionesName = "Ninguno";
        String oldTerritoriosID = "Ninguno";String oldTerritoriosName = "Ninguno";

        // 2. LEER ESTADO VIEJO DESDE LA BD (Antes de que se ejecute el update)
        var opt = proyectoRepository.findById(idProyecto);
        if (opt.isPresent()) {
            Proyecto p = opt.get();
            oldNombre = p.getNombre();
            oldCodigo = p.getCodigoInterno();
            oldDesc = p.getDescripcion();
            oldObj = p.getObjetivoGeneral();
            oldInicio = p.getFechaInicio() != null ? p.getFechaInicio().toString() : null;
            oldFin = p.getFechaFinEstimada() != null ? p.getFechaFinEstimada().toString() : null;
            oldEstado = p.getEstado() != null ? p.getEstado().name() : null;
            oldPrioridad = p.getNivelPrioridad() != null ? String.valueOf(p.getNivelPrioridad()) : null;
            oldAvance = p.getPorcentajeAvance() != null ? String.valueOf(p.getPorcentajeAvance()) : null;
            oldPresupuesto = p.getPresupuesto() != null ? String.valueOf(p.getPresupuesto()) : null;

            // Extraer IDs de Relaciones Complejas (Evita el LazyInitializationException extrayendo solo lo necesario)
            if (p.getEjeTematico() != null){
                oldEjeTematicoID = p.getEjeTematico().getId().toString();
                oldEjeTematicoName = p.getEjeTematico().getNombre();
            }
            if (p.getResponsablePrincipal() != null){
                oldResponsableID = p.getResponsablePrincipal().getId().toString();
                oldResponsableName = p.getResponsablePrincipal().getNombres();
            }

            if (p.getMacroregiones() != null && !p.getMacroregiones().isEmpty()) {
                oldMacroregionesID = p.getMacroregiones().stream()
                        .map(m -> m.getId().toString()).sorted().collect(Collectors.joining(","));
                oldMacroregionesName = p.getMacroregiones().stream()
                        .map(Macroregion::getNombre).sorted().collect(Collectors.joining(","));
            }
            if (p.getTerritorios() != null && !p.getTerritorios().isEmpty()) {
                oldTerritoriosID = p.getTerritorios().stream()
                        .map(t -> t.getId().toString()).sorted().collect(Collectors.joining(","));
                oldTerritoriosName = p.getTerritorios().stream()
                        .map(Territorio::getNombre).sorted().collect(Collectors.joining(","));
            }
        }


        // 3. EJECUTAR LA ACTUALIZACIÓN EN EL SERVICIO (El proceed guarda los datos nuevos en BD)
        Object result = joinPoint.proceed();

        // 4. COMPARAR CAMPO POR CAMPO
        List<String> cambios = new ArrayList<>();

        // --- Atributos de Texto ---
        if (!Objects.equals(oldNombre, dto.nombre())) {
            cambios.add(String.format("Nombre ['%s' -> '%s']", oldNombre, dto.nombre()));
        }
        if (!Objects.equals(oldCodigo, dto.codigoInterno())) {
            cambios.add(String.format("Código ['%s' -> '%s']", oldCodigo, dto.codigoInterno()));
        }
        // Para textos largos como Descripción u Objetivo, es mejor no imprimir todo el párrafo en la bitácora
        if (!Objects.equals(oldDesc, dto.descripcion())) {
            cambios.add("Descripción [Texto modificado]");
        }
        if (!Objects.equals(oldObj, dto.objetivoGeneral())) {
            cambios.add("Objetivo General [Texto modificado]");
        }

        // --- Atributos de Fechas ---
        String newInicio = dto.fechaInicio() != null ? dto.fechaInicio().toString() : null;
        if (!Objects.equals(oldInicio, newInicio)) {
            cambios.add(String.format("Fecha Inicio ['%s' -> '%s']", oldInicio, newInicio));
        }
        String newFin = dto.fechaFinEstimada() != null ? dto.fechaFinEstimada().toString() : null;
        if (!Objects.equals(oldFin, newFin)) {
            cambios.add(String.format("Fecha Fin ['%s' -> '%s']", oldFin, newFin));
        }

        // --- Atributos Numéricos y Enums ---
        String newEstado = dto.estado() != null ? dto.estado().name() : null;
        if (!Objects.equals(oldEstado, newEstado) && newEstado != null) {
            cambios.add(String.format("Estado ['%s' -> '%s']", oldEstado, newEstado));
        }
        String newPrioridad = dto.nivelPrioridad() != null ? String.valueOf(dto.nivelPrioridad()) : null;
        if (!Objects.equals(oldPrioridad, newPrioridad)) {
            cambios.add(String.format("Prioridad ['%s' -> '%s']", oldPrioridad, newPrioridad));
        }
        String newAvance = dto.porcentajeAvance() != null ? String.valueOf(dto.porcentajeAvance()) : null;
        if (!Objects.equals(oldAvance, newAvance) && newAvance != null) {
            cambios.add(String.format("Avance ['%s%%' -> '%s%%']", oldAvance, newAvance));
        }
        String newPresu = dto.presupuesto() != null ? String.valueOf(dto.presupuesto()) : null;
        if (!Objects.equals(oldPresupuesto, newPresu)) {
            cambios.add(String.format("Presupuesto ['%s' -> '%s']", oldPresupuesto, newPresu));
        }

        // --- Entidades Complejas (Comparadas por ID) ---
        /* * IMPLEMENTACIÓN FUTURA (NOMBRES EN LUGAR DE IDs):
         * Si deseas que en la bitácora salga "Responsable ['Juan' -> 'Pedro']" en lugar de "['1' -> '2']",
         * debes inyectar UsuarioRepository y EjeTematicoRepository en este Aspecto, y hacer un .findById(dto.idResponsablePrincipal())
         * aquí mismo para extraer el nombre real y concatenarlo.
         */

        String newEjeID = dto.idEjeTematico() != null ? dto.idEjeTematico().toString() : "Ninguno";
        String newEjeName = dto.idEjeTematico() != null ? dto.nombre() : "Ninguno";
        if (!Objects.equals(oldEjeTematicoID, newEjeID)) {
            cambios.add(String.format("Eje Temático (ID) ['%s' -> '%s']", oldEjeTematicoName, newEjeName));
        }

        String newResponsableID = dto.idResponsablePrincipal() != null ? dto.idResponsablePrincipal().toString() : "Ninguno";
        String newResponsableName = dto.idResponsablePrincipal() != null ? dto.nombre() : "Ninguno";
        if (!Objects.equals(oldResponsableID, newResponsableID)) {
            cambios.add(String.format("Responsable Principal (ID) ['%s' -> '%s']", oldResponsableName, newResponsableName));
        }

        // Colecciones (Sets) - Ordenamos los IDs y los unimos por comas para comparar las listas exactas
        String newMacroregionesID = "Ninguna";
        String newMacroregionesName = "Ninguna";
        if (dto.idMacroregiones() != null && !dto.idMacroregiones().isEmpty()) {
            newMacroregionesName = macroregionRepository.findAllById(dto.idMacroregiones()).stream().map(Macroregion::getNombre)
                    .sorted().collect(Collectors.joining(", "));
        }
        if (!Objects.equals(oldMacroregionesID, newMacroregionesID)) {
            cambios.add(String.format("Macrorregiones (IDs) ['%s' -> '%s']", oldMacroregionesName, newMacroregionesName));
        }

        String newTerritoriosID = "Ninguno";
        String newTerritoriosName = "Ninguno";
        if (dto.idTerritorios() != null && !dto.idTerritorios().isEmpty()) {
            newTerritoriosID = territorioRepository.findAllById(dto.idTerritorios()).stream().map(Territorio::getNombre)
                    .sorted().collect(Collectors.joining(", "));
        }
        if (!Objects.equals(oldTerritoriosID, newTerritoriosID)) {
            cambios.add(String.format("Territorios (IDs) ['%s' -> '%s']", oldTerritoriosName, newTerritoriosName));
        }

        // 5. REGISTRAR SOLO SI HUBO CAMBIOS REALES
        if (!cambios.isEmpty()) {
            String descripcionFinal = "Se actualizaron los datos: \n" + String.join("\n", cambios);

            // Asumiendo que usas tu método registrarSeguro
            registrarSeguro(MODIFICACION, ENTIDAD_PROYECTO, idProyecto, descripcionFinal);
        }

        return result;
    }
}
