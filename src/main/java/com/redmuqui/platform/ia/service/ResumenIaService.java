package com.redmuqui.platform.ia.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.ia.client.LlmClient;
import com.redmuqui.platform.ia.dto.ResumenIaResponse;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Genera el Resumen Ejecutivo con IA de un proyecto (Sprint 4 ⑤).
 *
 * <p>Estrategia anti-alucinación (grounding): el servicio NO le pide al modelo
 * que "conozca" el proyecto. Arma una ficha factual con datos reales de la BD
 * (mismas cifras que alimentan dashboard y semáforo) y le instruye redactar
 * usando exclusivamente esos datos. Si no hay proveedor de IA configurado o la
 * llamada falla, devuelve un resumen-plantilla derivado de la misma ficha, así
 * la funcionalidad nunca rompe y es transparente sobre su origen.</p>
 */
@Service
@RequiredArgsConstructor
public class ResumenIaService {

    private static final Logger log = LoggerFactory.getLogger(ResumenIaService.class);

    private static final String MODELO_PLANTILLA = "plantilla-local";

    /** Mismos umbrales de riesgo que el dashboard/semáforo (RF-071), para coherencia. */
    static final long UMBRAL_DIAS_RIESGO = 30L;
    static final double UMBRAL_AVANCE_RIESGO = 70.0;

    private static final String INSTRUCCION_SISTEMA = """
        Eres analista de la organización peruana Red Muqui. Redacta un RESUMEN EJECUTIVO del proyecto, \
        en español, dirigido a la dirección y a cooperantes/donantes. Reglas:
        - Usa EXCLUSIVAMENTE los datos de la ficha. No inventes cifras, nombres ni hechos; si un dato no aparece, no lo menciones.
        - 1 o 2 párrafos, máximo ~140 palabras, en prosa continua (sin viñetas ni encabezados), tono profesional y claro.
        - Destaca el avance, la ejecución presupuestal y los beneficiarios con enfoque de equidad de género (hombres y mujeres).
        - Menciona los logros (hitos y actividades finalizados) y, si existen, las alertas de riesgo.
        - No incluyas saludos ni despedidas: entrega solo el resumen.""";

    private final ProyectoRepository proyectoRepository;
    private final ActividadRepository actividadRepository;
    private final HitoRepository hitoRepository;
    private final LlmClient llmClient;

    @Transactional(readOnly = true)
    public ResumenIaResponse generarResumen(Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", idProyecto));

        Ficha ficha = construirFicha(proyecto);

        if (!llmClient.estaConfigurado()) {
            return respuesta(ficha, resumenPlantilla(ficha), false, MODELO_PLANTILLA,
                "Resumen generado automáticamente: el servicio de IA no está configurado.");
        }

        try {
            String texto = llmClient.generar(INSTRUCCION_SISTEMA, formatearFicha(ficha));
            return respuesta(ficha, texto, true, llmClient.modelo(), null);
        } catch (Exception e) {
            log.warn("No se pudo generar el resumen con IA para el proyecto {}: {}", idProyecto, e.getMessage());
            return respuesta(ficha, resumenPlantilla(ficha), false, MODELO_PLANTILLA,
                "No se pudo contactar al servicio de IA; se muestra un resumen automático.");
        }
    }

    private ResumenIaResponse respuesta(Ficha f, String resumen, boolean ia, String modelo, String aviso) {
        return new ResumenIaResponse(
            f.id(), f.nombre(), f.codigoInterno(),
            resumen == null ? "" : resumen.strip(),
            ia, modelo, aviso, LocalDateTime.now()
        );
    }

    // ───────────────────────── Ficha factual ─────────────────────────

    private Ficha construirFicha(Proyecto p) {
        LocalDate hoy = LocalDate.now();

        List<Actividad> actividades = actividadRepository.findByProyectoId(p.getId());
        List<Hito> hitos = hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(p.getId());

        long actFinalizadas = actividades.stream().filter(a -> a.getEstado() == EstadoActividad.FINALIZADA).count();
        long actEnCurso = actividades.stream().filter(a -> a.getEstado() == EstadoActividad.EN_CURSO).count();
        long actPendientes = actividades.stream().filter(a -> a.getEstado() == EstadoActividad.PENDIENTE).count();
        long actVencidas = actividades.stream()
            .filter(a -> a.getEstado() != EstadoActividad.FINALIZADA
                && a.getFechaFin() != null && a.getFechaFin().isBefore(hoy))
            .count();

        long hitosFinalizados = hitos.stream().filter(h -> h.getEstado() == EstadoHito.FINALIZADO).count();
        long hitosVencidos = hitos.stream()
            .filter(h -> h.getEstado() != EstadoHito.FINALIZADO && h.getFechaClave().isBefore(hoy))
            .count();
        String proximosHitos = hitos.stream()
            .filter(h -> h.getEstado() != EstadoHito.FINALIZADO && !h.getFechaClave().isBefore(hoy))
            .limit(3)
            .map(h -> h.getNombre() + " (" + h.getFechaClave() + ")")
            .collect(Collectors.joining("; "));

        long hombres = 0L;
        long mujeres = 0L;
        double presupuestoEjecutado = 0.0;
        for (Actividad a : actividades) {
            for (Subactividad s : a.getSubactividades()) {
                hombres += s.getHombresInvolucrados() == null ? 0 : s.getHombresInvolucrados();
                mujeres += s.getMujeresInvolucradas() == null ? 0 : s.getMujeresInvolucradas();
                presupuestoEjecutado += s.getPresupuesto() == null ? 0.0 : s.getPresupuesto();
            }
        }

        Long diasRestantes = p.getFechaFinEstimada() == null ? null
            : ChronoUnit.DAYS.between(hoy, p.getFechaFinEstimada());
        double avance = p.getPorcentajeAvance() == null ? 0.0 : p.getPorcentajeAvance();
        boolean enRiesgo = hitosVencidos > 0
            || (diasRestantes != null && diasRestantes <= UMBRAL_DIAS_RIESGO && avance < UMBRAL_AVANCE_RIESGO);

        String macroregiones = p.getMacroregiones().stream()
            .map(m -> m.getNombre()).collect(Collectors.joining(", "));
        String territorios = p.getTerritorios().stream()
            .map(t -> t.getNombre()).collect(Collectors.joining(", "));
        String instituciones = p.getInstituciones().stream()
            .map(pi -> pi.getInstitucion().getNombre()).collect(Collectors.joining(", "));
        String eje = p.getEjeTematico() == null ? null : p.getEjeTematico().getNombre();
        String responsable = p.getResponsablePrincipal() == null ? null
            : (p.getResponsablePrincipal().getNombres() + " " + p.getResponsablePrincipal().getApellidos()).trim();

        return new Ficha(
            p.getId(), p.getNombre(), p.getCodigoInterno(), p.getEstado().name(),
            avance, p.getPresupuesto(), presupuestoEjecutado,
            p.getFechaInicio(), p.getFechaFinEstimada(), diasRestantes,
            eje, macroregiones, territorios, responsable, p.getInstituciones().size(), instituciones,
            p.getObjetivoGeneral(),
            actividades.size(), actFinalizadas, actEnCurso, actPendientes, actVencidas,
            hitos.size(), hitosFinalizados, hitosVencidos, proximosHitos,
            hombres, mujeres, enRiesgo
        );
    }

    /** Serializa la ficha como texto etiquetado para el prompt (grounding). */
    private String formatearFicha(Ficha f) {
        StringBuilder sb = new StringBuilder();
        sb.append("FICHA DEL PROYECTO (datos reales; no inventes nada fuera de esto):\n");
        sb.append("- Nombre: ").append(f.nombre()).append(" (código ").append(f.codigoInterno()).append(")\n");
        sb.append("- Estado: ").append(f.estado().toLowerCase(Locale.ROOT)).append("\n");
        sb.append("- Avance reportado: ").append(formatPct(f.avance())).append("\n");
        sb.append("- Periodo: ").append(f.fechaInicio()).append(" a ")
            .append(f.fechaFinEstimada() == null ? "sin fecha de fin definida" : f.fechaFinEstimada());
        if (f.diasRestantes() != null) {
            sb.append(f.diasRestantes() < 0
                ? " (plazo vencido hace " + Math.abs(f.diasRestantes()) + " días)"
                : " (" + f.diasRestantes() + " días restantes)");
        }
        sb.append("\n");
        sb.append("- Presupuesto del proyecto: ").append(formatMoneda(f.presupuesto())).append("\n");
        sb.append("- Presupuesto ejecutado en subactividades: ").append(formatMoneda(f.presupuestoEjecutado())).append("\n");
        sb.append("- Eje temático: ").append(siVacio(f.eje(), "no especificado")).append("\n");
        sb.append("- Macrorregiones: ").append(siVacio(f.macroregiones(), "ninguna")).append("\n");
        sb.append("- Territorios: ").append(siVacio(f.territorios(), "ninguno")).append("\n");
        sb.append("- Responsable principal: ").append(siVacio(f.responsable(), "no asignado")).append("\n");
        sb.append("- Instituciones participantes: ").append(f.numInstituciones());
        if (!f.instituciones().isBlank()) {
            sb.append(" (").append(f.instituciones()).append(")");
        }
        sb.append("\n");
        if (f.objetivoGeneral() != null && !f.objetivoGeneral().isBlank()) {
            sb.append("- Objetivo general: ").append(f.objetivoGeneral()).append("\n");
        }
        sb.append("- Actividades: ").append(f.totalActividades())
            .append(" (finalizadas ").append(f.actFinalizadas())
            .append(", en curso ").append(f.actEnCurso())
            .append(", pendientes ").append(f.actPendientes())
            .append(", vencidas ").append(f.actVencidas()).append(")\n");
        sb.append("- Hitos: ").append(f.totalHitos())
            .append(" (finalizados ").append(f.hitosFinalizados())
            .append(", vencidos ").append(f.hitosVencidos()).append(")\n");
        if (!f.proximosHitos().isBlank()) {
            sb.append("- Próximos hitos: ").append(f.proximosHitos()).append("\n");
        }
        sb.append("- Beneficiarios involucrados: ").append(f.beneficiariosTotal())
            .append(" (hombres ").append(f.beneficiariosHombres())
            .append(", mujeres ").append(f.beneficiariosMujeres()).append(")\n");
        sb.append("- Clasificación de riesgo: ").append(f.enRiesgo()
            ? "EN RIESGO (hitos vencidos o plazo próximo con bajo avance)"
            : "sin alertas de riesgo").append("\n");
        return sb.toString();
    }

    /** Resumen determinista a partir de la ficha, para cuando no hay IA disponible. */
    private String resumenPlantilla(Ficha f) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(String.format("El proyecto «%s» (%s) se encuentra %s con un avance reportado del %s.",
            f.nombre(), f.codigoInterno(), f.estado().toLowerCase(Locale.ROOT), formatPct(f.avance())));

        if (f.presupuesto() != null) {
            sj.add(String.format("Cuenta con un presupuesto de %s y una ejecución registrada de %s en subactividades.",
                formatMoneda(f.presupuesto()), formatMoneda(f.presupuestoEjecutado())));
        }

        if (f.beneficiariosTotal() > 0) {
            sj.add(String.format("Ha involucrado a %d beneficiarios (%d hombres y %d mujeres).",
                f.beneficiariosTotal(), f.beneficiariosHombres(), f.beneficiariosMujeres()));
        }

        if (f.totalHitos() > 0 || f.totalActividades() > 0) {
            sj.add(String.format("Registra %d de %d hitos finalizados y %d de %d actividades completadas.",
                f.hitosFinalizados(), f.totalHitos(), f.actFinalizadas(), f.totalActividades()));
        }

        if (f.enRiesgo()) {
            sj.add(String.format("Presenta alertas de riesgo: %d hito(s) vencido(s)%s.",
                f.hitosVencidos(),
                f.diasRestantes() != null && f.diasRestantes() <= UMBRAL_DIAS_RIESGO
                    ? " y plazo próximo a vencer" : ""));
        } else {
            sj.add("No presenta alertas de riesgo en plazos ni hitos.");
        }
        return sj.toString();
    }

    private static String formatPct(double v) {
        return String.format(Locale.US, "%.0f%%", v);
    }

    private static String formatMoneda(Double v) {
        if (v == null) {
            return "no definido";
        }
        return "S/ " + String.format(Locale.US, "%,.0f", v);
    }

    private static String siVacio(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v;
    }

    /** Foto factual del proyecto sobre la que se redacta el resumen. */
    private record Ficha(
        Long id, String nombre, String codigoInterno, String estado,
        double avance, Double presupuesto, double presupuestoEjecutado,
        LocalDate fechaInicio, LocalDate fechaFinEstimada, Long diasRestantes,
        String eje, String macroregiones, String territorios, String responsable,
        long numInstituciones, String instituciones, String objetivoGeneral,
        long totalActividades, long actFinalizadas, long actEnCurso, long actPendientes, long actVencidas,
        long totalHitos, long hitosFinalizados, long hitosVencidos, String proximosHitos,
        long beneficiariosHombres, long beneficiariosMujeres, boolean enRiesgo
    ) {
        long beneficiariosTotal() {
            return beneficiariosHombres + beneficiariosMujeres;
        }
    }
}
