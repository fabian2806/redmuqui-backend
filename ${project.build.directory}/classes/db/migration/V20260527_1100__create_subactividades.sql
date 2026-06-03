-- ============================
-- V20260527_1100__create_subactividades.sql
-- Creación de tablas para subactividades y cofinanciamiento
-- ============================

-- 1. Agregar campo porcentaje_avance a actividades
ALTER TABLE actividades ADD COLUMN porcentaje_avance INTEGER DEFAULT 0;

-- 2. Tabla de Subactividades
CREATE TABLE subactividades (
    id                      BIGSERIAL PRIMARY KEY,
    nombre                  VARCHAR(255) NOT NULL,
    id_responsable          BIGINT NOT NULL REFERENCES usuarios(id),
    presupuesto             DOUBLE PRECISION DEFAULT 0.0,
    hombres_involucrados    INTEGER DEFAULT 0,
    mujeres_involucradas    INTEGER DEFAULT 0,
    fecha_inicio            DATE,
    fecha_fin               DATE,
    descripcion             TEXT,
    id_actividad            BIGINT NOT NULL REFERENCES actividades(id) ON DELETE CASCADE,
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion      TIMESTAMP
);

-- 3. Tabla para evidencias/archivos adjuntos
CREATE TABLE subactividad_archivos (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    url                 VARCHAR(500) NOT NULL,
    id_subactividad     BIGINT NOT NULL REFERENCES subactividades(id) ON DELETE CASCADE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla para cofinanciamientos
CREATE TABLE subactividad_cofinanciamiento (
    id_subactividad     BIGINT NOT NULL REFERENCES subactividades(id) ON DELETE CASCADE,
    id_actividad_origen BIGINT NOT NULL REFERENCES actividades(id) ON DELETE RESTRICT,
    monto               DOUBLE PRECISION NOT NULL CHECK (monto > 0),
    PRIMARY KEY (id_subactividad, id_actividad_origen)
);
