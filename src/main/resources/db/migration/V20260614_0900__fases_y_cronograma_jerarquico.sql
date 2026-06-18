-- Reestructura el cronograma como Proyecto -> Fase -> Actividad -> Subactividad.
-- Los hitos pasan a ser puntos de control dentro de una fase.

ALTER TABLE actividades RENAME COLUMN fecha_inicio TO fecha_inicio_planificada;
ALTER TABLE actividades RENAME COLUMN fecha_fin TO fecha_fin_planificada;
ALTER TABLE subactividades RENAME COLUMN fecha_inicio TO fecha_inicio_planificada;
ALTER TABLE subactividades RENAME COLUMN fecha_fin TO fecha_fin_planificada;

CREATE TABLE fases (
    id                       BIGSERIAL PRIMARY KEY,
    nombre                   VARCHAR(255) NOT NULL,
    descripcion              TEXT,
    fecha_inicio_planificada DATE NOT NULL,
    fecha_fin_planificada    DATE NOT NULL,
    fecha_inicio_real        DATE,
    fecha_fin_real           DATE,
    estado                   VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    porcentaje_avance        DOUBLE PRECISION NOT NULL DEFAULT 0,
    id_proyecto              BIGINT NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    legacy_hito_id           BIGINT,
    fecha_creacion           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion       TIMESTAMP
);

CREATE INDEX idx_fases_proyecto ON fases(id_proyecto);

ALTER TABLE actividades ADD COLUMN id_fase BIGINT;
ALTER TABLE hitos ADD COLUMN id_fase BIGINT;

-- Cada hito anterior actuaba como contenedor. Se convierte en una fase inicial.
INSERT INTO fases (
    nombre,
    descripcion,
    fecha_inicio_planificada,
    fecha_fin_planificada,
    estado,
    porcentaje_avance,
    id_proyecto,
    legacy_hito_id,
    fecha_creacion,
    fecha_modificacion
)
SELECT
    'Fase - ' || h.nombre,
    h.descripcion,
    COALESCE(MIN(a.fecha_inicio_planificada), p.fecha_inicio),
    COALESCE(MAX(a.fecha_fin_planificada), h.fecha_clave, p.fecha_fin_estimada),
    CASE
        WHEN h.estado = 'FINALIZADO' THEN 'FINALIZADA'
        WHEN h.estado = 'EN_CURSO' THEN 'EN_CURSO'
        ELSE 'PENDIENTE'
    END,
    COALESCE(AVG(a.porcentaje_avance), 0),
    h.id_proyecto,
    h.id,
    h.fecha_creacion,
    h.fecha_modificacion
FROM hitos h
JOIN proyectos p ON p.id = h.id_proyecto
LEFT JOIN actividades a ON a.id_hito = h.id
GROUP BY h.id, p.fecha_inicio, p.fecha_fin_estimada;

UPDATE hitos h
SET id_fase = f.id
FROM fases f
WHERE f.legacy_hito_id = h.id;

UPDATE actividades a
SET id_fase = h.id_fase
FROM hitos h
WHERE a.id_hito = h.id;

-- Proyectos con actividades no clasificadas reciben una fase general.
INSERT INTO fases (
    nombre,
    descripcion,
    fecha_inicio_planificada,
    fecha_fin_planificada,
    id_proyecto
)
SELECT
    'Fase general',
    'Fase creada durante la migración para actividades históricas sin clasificación.',
    COALESCE(MIN(a.fecha_inicio_planificada), p.fecha_inicio),
    COALESCE(MAX(a.fecha_fin_planificada), p.fecha_fin_estimada),
    p.id
FROM proyectos p
JOIN actividades a ON a.id_proyecto = p.id AND a.id_fase IS NULL
WHERE NOT EXISTS (SELECT 1 FROM fases f WHERE f.id_proyecto = p.id)
GROUP BY p.id, p.fecha_inicio, p.fecha_fin_estimada;

UPDATE actividades a
SET id_fase = (
    SELECT f.id
    FROM fases f
    WHERE f.id_proyecto = a.id_proyecto
    ORDER BY f.fecha_inicio_planificada, f.id
    LIMIT 1
)
WHERE a.id_fase IS NULL;

ALTER TABLE actividades
    ADD CONSTRAINT fk_actividades_fase
    FOREIGN KEY (id_fase) REFERENCES fases(id) ON DELETE RESTRICT;

ALTER TABLE hitos
    ADD CONSTRAINT fk_hitos_fase
    FOREIGN KEY (id_fase) REFERENCES fases(id) ON DELETE CASCADE;

CREATE INDEX idx_actividades_id_fase ON actividades(id_fase);
CREATE INDEX idx_hitos_id_fase ON hitos(id_fase);

ALTER TABLE actividades ALTER COLUMN id_fase SET NOT NULL;
ALTER TABLE hitos ALTER COLUMN id_fase SET NOT NULL;
ALTER TABLE fases DROP COLUMN legacy_hito_id;
