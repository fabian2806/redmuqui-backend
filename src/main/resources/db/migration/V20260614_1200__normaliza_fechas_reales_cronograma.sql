-- Corrige fechas reales inferidas con la fecha de carga/edición aunque la
-- ejecución planificada todavía no hubiera comenzado.
UPDATE subactividades
SET fecha_inicio_real = fecha_inicio_planificada
WHERE estado IN ('EN_CURSO', 'FINALIZADA')
  AND (
    fecha_inicio_real IS NULL
    OR fecha_inicio_real < fecha_inicio_planificada
    OR (fecha_fin_real IS NOT NULL AND fecha_inicio_real > fecha_fin_real)
  );

-- Actividades y fases obtienen sus fechas reales exclusivamente de sus hijos.
UPDATE actividades a
SET fecha_inicio_real = resumen.fecha_inicio_real,
    fecha_fin_real = CASE
      WHEN resumen.total = resumen.finalizadas THEN resumen.fecha_fin_real
      ELSE NULL
    END
FROM (
  SELECT
    id_actividad,
    MIN(fecha_inicio_real) AS fecha_inicio_real,
    MAX(fecha_fin_real) AS fecha_fin_real,
    COUNT(*) AS total,
    COUNT(*) FILTER (WHERE estado = 'FINALIZADA') AS finalizadas
  FROM subactividades
  GROUP BY id_actividad
) resumen
WHERE a.id = resumen.id_actividad;

UPDATE fases f
SET fecha_inicio_real = resumen.fecha_inicio_real,
    fecha_fin_real = CASE
      WHEN resumen.total = resumen.finalizadas THEN resumen.fecha_fin_real
      ELSE NULL
    END
FROM (
  SELECT
    id_fase,
    MIN(fecha_inicio_real) AS fecha_inicio_real,
    MAX(fecha_fin_real) AS fecha_fin_real,
    COUNT(*) AS total,
    COUNT(*) FILTER (WHERE estado = 'FINALIZADA') AS finalizadas
  FROM actividades
  GROUP BY id_fase
) resumen
WHERE f.id = resumen.id_fase;
