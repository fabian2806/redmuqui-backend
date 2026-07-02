CREATE TABLE estados (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    modulo VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    CONSTRAINT uk_estado_nombre_modulo UNIQUE (nombre, modulo)
);

CREATE TABLE tipos_documento (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL UNIQUE,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP
);

CREATE TABLE monedas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL UNIQUE,
    codigo VARCHAR(3) NOT NULL UNIQUE,
    simbolo VARCHAR(10) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP
);

-- Seed data: monedas comunes
INSERT INTO monedas (nombre, codigo, simbolo, activo) VALUES
    ('Sol peruano', 'PEN', 'S/', TRUE),
    ('Dólar estadounidense', 'USD', '$', TRUE),
    ('Euro', 'EUR', '€', TRUE);

-- Seed data: tipos de documento iniciales
INSERT INTO tipos_documento (nombre, codigo, descripcion, activo) VALUES
    ('Informe', 'INFORME', NULL, TRUE),
    ('Pronunciamiento', 'PRONUNCIAMIENTO', NULL, TRUE),
    ('Investigación', 'INVESTIGACION', NULL, TRUE),
    ('Manual', 'MANUAL', NULL, TRUE),
    ('Cartilla', 'CARTILLA', NULL, TRUE),
    ('Resumen técnico', 'RESUMEN_TECNICO', NULL, TRUE);

-- Seed data: estados por módulo
INSERT INTO estados (nombre, codigo, descripcion, modulo, activo) VALUES
    ('Activo', 'ACTIVO', 'El recurso está activo', 'PROYECTO', TRUE),
    ('Cerrado', 'CERRADO', 'El recurso está cerrado', 'PROYECTO', TRUE),
    ('Suspendido', 'SUSPENDIDO', 'El recurso está suspendido', 'PROYECTO', TRUE),
    ('Borrador', 'BORRADOR', 'El documento está en borrador', 'DOCUMENTO', TRUE),
    ('En revisión', 'EN_REVISION', 'El documento está en revisión', 'DOCUMENTO', TRUE),
    ('Publicado', 'PUBLICADO', 'El documento está publicado', 'DOCUMENTO', TRUE),
    ('Pendiente', 'PENDIENTE', 'La actividad está pendiente', 'ACTIVIDAD', TRUE),
    ('En curso', 'EN_CURSO', 'La actividad está en curso', 'ACTIVIDAD', TRUE),
    ('Finalizada', 'FINALIZADA', 'La actividad está finalizada', 'ACTIVIDAD', TRUE),
    ('Vencida', 'VENCIDA', 'La actividad está vencida', 'ACTIVIDAD', TRUE);
