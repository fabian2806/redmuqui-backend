-- Presupuesto propio de actividades y trazabilidad de cofinanciamientos.

ALTER TABLE actividades
    ADD COLUMN presupuesto DOUBLE PRECISION NOT NULL DEFAULT 0;

ALTER TABLE subactividad_cofinanciamiento
    ADD COLUMN justificacion TEXT;

UPDATE subactividad_cofinanciamiento
SET justificacion = 'Registro previo sin justificacion detallada'
WHERE justificacion IS NULL;

ALTER TABLE subactividad_cofinanciamiento
    ALTER COLUMN justificacion SET NOT NULL;
