-- Fechas reales y trazabilidad de reprogramaciones.
ALTER TABLE hitos
    ADD COLUMN fecha_cumplimiento_real DATE;

ALTER TABLE actividades
    ADD COLUMN fecha_inicio_real DATE,
    ADD COLUMN fecha_fin_real DATE;

ALTER TABLE subactividades
    ADD COLUMN fecha_inicio_real DATE,
    ADD COLUMN fecha_fin_real DATE;

CREATE TABLE cronograma_reprogramaciones (
    id                    BIGSERIAL PRIMARY KEY,
    tipo_entidad          VARCHAR(30) NOT NULL,
    id_entidad            BIGINT NOT NULL,
    fecha_inicio_anterior DATE,
    fecha_fin_anterior    DATE,
    fecha_inicio_nueva    DATE,
    fecha_fin_nueva       DATE,
    motivo                VARCHAR(500) NOT NULL,
    id_usuario            BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_creacion        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cronograma_reprogramacion_entidad
    ON cronograma_reprogramaciones(tipo_entidad, id_entidad, fecha_creacion DESC);

-- El levantamiento de una incidencia debe dejar evidencia de quién y cómo la resolvió.
ALTER TABLE observaciones
    ADD COLUMN comentario_resolucion TEXT,
    ADD COLUMN id_usuario_resolucion BIGINT REFERENCES usuarios(id);
