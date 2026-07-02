package com.redmuqui.platform.ia.dto;

/**
 * Métricas numéricas del proyecto que acompañan al resumen ejecutivo (Sprint 4 ⑤).
 *
 * <p>Salen de la MISMA ficha factual con la que se redacta el texto, así los
 * gráficos del frontend y la narrativa de la IA siempre muestran las mismas
 * cifras (coherencia y anti-alucinación). Se incluyen tanto si el resumen lo
 * redactó la IA como si es la plantilla local.</p>
 *
 * @param avance                porcentaje de avance físico reportado (0-100)
 * @param presupuestoTotal      presupuesto del proyecto ({@code null} si no está definido)
 * @param presupuestoEjecutado  suma ejecutada en subactividades
 * @param beneficiariosHombres  beneficiarios hombres involucrados
 * @param beneficiariosMujeres  beneficiarias mujeres involucradas
 * @param actividadesFinalizadas actividades finalizadas
 * @param actividadesEnCurso    actividades en curso
 * @param actividadesPendientes actividades pendientes
 * @param actividadesVencidas   actividades fuera de plazo (no finalizadas y vencidas)
 * @param actividadesTotal      total de actividades
 * @param hitosTotal            total de hitos
 * @param hitosFinalizados      hitos finalizados
 * @param hitosVencidos         hitos vencidos (no finalizados y con fecha clave pasada)
 * @param enRiesgo              {@code true} si el proyecto tiene alertas de riesgo (RF-071)
 */
public record MetricasResumen(
    double avance,
    Double presupuestoTotal,
    double presupuestoEjecutado,
    long beneficiariosHombres,
    long beneficiariosMujeres,
    long actividadesFinalizadas,
    long actividadesEnCurso,
    long actividadesPendientes,
    long actividadesVencidas,
    long actividadesTotal,
    long hitosTotal,
    long hitosFinalizados,
    long hitosVencidos,
    boolean enRiesgo
) {}
