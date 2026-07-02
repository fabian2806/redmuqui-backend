package com.redmuqui.platform.ia.dto;

import java.time.LocalDateTime;

/**
 * Resumen ejecutivo de un proyecto generado para la dirección y cooperantes (Sprint 4 ⑤).
 *
 * @param idProyecto    id del proyecto resumido
 * @param nombreProyecto nombre del proyecto
 * @param codigoInterno código interno del proyecto
 * @param resumen       texto narrativo del resumen
 * @param generadoPorIa {@code true} si lo redactó el modelo de IA; {@code false} si es la plantilla local
 * @param modelo        modelo usado (p.ej. {@code gemini-2.0-flash}) o {@code plantilla-local}
 * @param aviso         mensaje opcional para el usuario (p.ej. por qué se usó la plantilla); {@code null} si no aplica
 * @param generadoEn    momento de generación
 * @param metricas      cifras del proyecto (avance, presupuesto, beneficiarios, actividades, hitos) para graficar
 */
public record ResumenIaResponse(
    Long idProyecto,
    String nombreProyecto,
    String codigoInterno,
    String resumen,
    boolean generadoPorIa,
    String modelo,
    String aviso,
    LocalDateTime generadoEn,
    MetricasResumen metricas
) {}
