-- Documentos: usuario de carga, versionado formal y comentarios de revisión.
ALTER TABLE documentos
    ADD COLUMN id_usuario_carga BIGINT REFERENCES usuarios(id);

UPDATE documentos
SET id_usuario_carga = id_resp_elaboracion
WHERE id_usuario_carga IS NULL;

ALTER TABLE documentos
    ALTER COLUMN id_usuario_carga SET NOT NULL;

ALTER TABLE archivos
    ADD COLUMN numero_version INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN id_usuario_carga BIGINT REFERENCES usuarios(id);

UPDATE archivos a
SET id_usuario_carga = d.id_usuario_carga
FROM documentos d
WHERE a.id_documento = d.id
  AND a.id_usuario_carga IS NULL;

ALTER TABLE archivos
    ALTER COLUMN id_usuario_carga SET NOT NULL;

CREATE TABLE documento_versiones (
    id                    BIGSERIAL PRIMARY KEY,
    id_documento          BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    numero_version        INTEGER NOT NULL,
    titulo                VARCHAR(255) NOT NULL,
    descripcion           TEXT,
    tipo                  VARCHAR(100),
    estado                VARCHAR(30) NOT NULL,
    id_proyecto           BIGINT REFERENCES proyectos(id),
    id_eje_tematico       BIGINT REFERENCES ejes_tematicos(id),
    id_resp_elaboracion   BIGINT NOT NULL REFERENCES usuarios(id),
    id_resp_validacion    BIGINT REFERENCES usuarios(id),
    id_usuario_cambio     BIGINT NOT NULL REFERENCES usuarios(id),
    motivo_cambio         VARCHAR(500),
    fecha_creacion        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_documento_version UNIQUE (id_documento, numero_version)
);

CREATE INDEX idx_documento_version_documento
    ON documento_versiones(id_documento, numero_version DESC);

CREATE TABLE documento_comentarios (
    id                    BIGSERIAL PRIMARY KEY,
    id_documento          BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    id_usuario            BIGINT NOT NULL REFERENCES usuarios(id),
    comentario            TEXT NOT NULL,
    fecha_creacion        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion    TIMESTAMP
);

CREATE INDEX idx_documento_comentario_documento
    ON documento_comentarios(id_documento, fecha_creacion DESC);

-- Proyectos y costos.
UPDATE proyectos
SET fecha_fin_estimada = fecha_inicio
WHERE fecha_fin_estimada IS NULL;

UPDATE proyectos
SET presupuesto = 0
WHERE presupuesto IS NULL;

ALTER TABLE proyectos
    ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    ALTER COLUMN fecha_fin_estimada SET NOT NULL,
    ALTER COLUMN presupuesto SET NOT NULL;

ALTER TABLE subactividades
    ADD COLUMN costo_real DOUBLE PRECISION,
    ADD COLUMN porcentaje_avance INTEGER NOT NULL DEFAULT 0;

ALTER TABLE subactividad_archivos
    ADD COLUMN estado VARCHAR(30) NOT NULL DEFAULT 'EN_REVISION',
    ADD COLUMN id_usuario_carga BIGINT REFERENCES usuarios(id);

UPDATE subactividad_archivos sa
SET id_usuario_carga = s.id_responsable
FROM subactividades s
WHERE sa.id_subactividad = s.id
  AND sa.id_usuario_carga IS NULL;

ALTER TABLE subactividad_archivos
    ALTER COLUMN id_usuario_carga SET NOT NULL;

-- Incidencias: criticidad, responsable, vencimiento y resolución.
ALTER TABLE observaciones
    ADD COLUMN criticidad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    ADD COLUMN fecha_vencimiento TIMESTAMP,
    ADD COLUMN id_responsable BIGINT REFERENCES usuarios(id),
    ADD COLUMN fecha_resolucion TIMESTAMP;

UPDATE observaciones
SET fecha_vencimiento = fecha + INTERVAL '7 days'
WHERE fecha_vencimiento IS NULL;

ALTER TABLE observaciones
    ALTER COLUMN fecha_vencimiento SET NOT NULL;

CREATE INDEX idx_observacion_estado_vencimiento
    ON observaciones(estado, fecha_vencimiento);
