-- =====================================================================
-- V20260508_0900__init.sql
-- Esquema inicial de la plataforma RedMuqui (20 tablas)
-- Generado a partir de las entidades JPA existentes y el diccionario
-- de datos (docs/Diccionario de datos DER.docx.pdf).
-- =====================================================================

-- ============================
-- 1. ROL
-- ============================
CREATE TABLE roles (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(50)  NOT NULL UNIQUE,
    descripcion         TEXT,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 2. PERMISO
-- ============================
CREATE TABLE permisos (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL UNIQUE,
    tipo                VARCHAR(50),
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 3. ROL_PERMISO  (N:M)
-- ============================
CREATE TABLE rol_permiso (
    id_rol     BIGINT NOT NULL REFERENCES roles(id)    ON DELETE CASCADE,
    id_permiso BIGINT NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
    PRIMARY KEY (id_rol, id_permiso)
);

-- ============================
-- 4. MACROREGION
-- ============================
CREATE TABLE macroregiones (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL UNIQUE,
    descripcion         TEXT,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 5. INSTITUCION
-- ============================
CREATE TABLE instituciones (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL UNIQUE,
    tipo                VARCHAR(100),
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 6. TERRITORIO
-- ============================
CREATE TABLE territorios (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL,
    descripcion         TEXT,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 7. EJE TEMATICO
-- ============================
CREATE TABLE ejes_tematicos (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL UNIQUE,
    descripcion         TEXT,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 8. USUARIO
-- ============================
CREATE TABLE usuarios (
    id                  BIGSERIAL    PRIMARY KEY,
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    contrasenha_hash    VARCHAR(255) NOT NULL,
    estado              BOOLEAN      NOT NULL DEFAULT TRUE,
    id_rol              BIGINT       NOT NULL REFERENCES roles(id),
    id_macroregion      BIGINT       REFERENCES macroregiones(id),
    id_institucion      BIGINT       REFERENCES instituciones(id),
    ultimo_acceso       TIMESTAMP,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);
CREATE UNIQUE INDEX idx_usuario_email ON usuarios(email);

-- ============================
-- 9. PROYECTO
-- ============================
CREATE TABLE proyectos (
    id                       BIGSERIAL    PRIMARY KEY,
    nombre                   VARCHAR(255) NOT NULL,
    codigo_interno           VARCHAR(50)  NOT NULL UNIQUE,
    descripcion              TEXT,
    objetivo_general         TEXT,
    fecha_inicio             DATE         NOT NULL,
    fecha_fin_estimada       DATE,
    estado                   VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO',
    nivel_prioridad          INTEGER,
    porcentaje_avance        DOUBLE PRECISION DEFAULT 0,
    presupuesto              DOUBLE PRECISION,
    id_macroregion           BIGINT REFERENCES macroregiones(id),
    id_eje_tematico          BIGINT REFERENCES ejes_tematicos(id),
    id_responsable_principal BIGINT REFERENCES usuarios(id),
    fecha_creacion           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion       TIMESTAMP
);
CREATE UNIQUE INDEX idx_proyecto_codigo ON proyectos(codigo_interno);

-- ============================
-- 10. PROYECTO_TERRITORIO  (N:M, sin atributos)
-- ============================
CREATE TABLE proyecto_territorio (
    id_proyecto   BIGINT NOT NULL REFERENCES proyectos(id)   ON DELETE CASCADE,
    id_territorio BIGINT NOT NULL REFERENCES territorios(id) ON DELETE CASCADE,
    PRIMARY KEY (id_proyecto, id_territorio)
);

-- ============================
-- 11. PROYECTO_EQUIPO  (N:M con rol_en_proyecto)
-- ============================
CREATE TABLE proyecto_equipo (
    id_proyecto      BIGINT       NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    id_usuario       BIGINT       NOT NULL REFERENCES usuarios(id),
    rol_en_proyecto  VARCHAR(100),
    PRIMARY KEY (id_proyecto, id_usuario)
);

-- ============================
-- 12. PROYECTO_INSTITUCION  (N:M con tipo_participacion)
-- ============================
CREATE TABLE proyecto_institucion (
    id_proyecto         BIGINT       NOT NULL REFERENCES proyectos(id)    ON DELETE CASCADE,
    id_institucion      BIGINT       NOT NULL REFERENCES instituciones(id),
    tipo_participacion  VARCHAR(100),
    PRIMARY KEY (id_proyecto, id_institucion)
);

-- ============================
-- 13. ACTIVIDAD
-- ============================
CREATE TABLE actividades (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    descripcion         TEXT,
    fecha_inicio        DATE,
    fecha_fin           DATE,
    estado              VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    id_proyecto         BIGINT       NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 14. ACTIVIDAD_RESPONSABLE  (N:M)
-- ============================
CREATE TABLE actividad_responsable (
    id_actividad BIGINT NOT NULL REFERENCES actividades(id) ON DELETE CASCADE,
    id_usuario   BIGINT NOT NULL REFERENCES usuarios(id),
    PRIMARY KEY (id_actividad, id_usuario)
);

-- ============================
-- 15. HITO
-- ============================
CREATE TABLE hitos (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    descripcion         TEXT,
    fecha_clave         DATE         NOT NULL,
    estado              VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    id_proyecto         BIGINT       NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 16. DOCUMENTO
-- ============================
CREATE TABLE documentos (
    id                   BIGSERIAL    PRIMARY KEY,
    titulo               VARCHAR(255) NOT NULL,
    descripcion          TEXT,
    tipo                 VARCHAR(100),
    estado               VARCHAR(30)  NOT NULL DEFAULT 'BORRADOR',
    tipo_archivo         VARCHAR(50),
    fecha_carga          DATE         NOT NULL DEFAULT CURRENT_DATE,
    enlace               VARCHAR(500),
    version              DOUBLE PRECISION DEFAULT 1.0,
    id_proyecto          BIGINT REFERENCES proyectos(id),
    id_eje_tematico      BIGINT REFERENCES ejes_tematicos(id),
    id_resp_elaboracion  BIGINT NOT NULL REFERENCES usuarios(id),
    id_resp_validacion   BIGINT REFERENCES usuarios(id),
    fecha_creacion       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   TIMESTAMP
);

-- ============================
-- 17. DOCUMENTO_TERRITORIO  (N:M)
-- ============================
CREATE TABLE documento_territorio (
    id_documento  BIGINT NOT NULL REFERENCES documentos(id)  ON DELETE CASCADE,
    id_territorio BIGINT NOT NULL REFERENCES territorios(id) ON DELETE CASCADE,
    PRIMARY KEY (id_documento, id_territorio)
);

-- ============================
-- 18. ARCHIVO
-- ============================
CREATE TABLE archivos (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    url                 VARCHAR(500) NOT NULL,
    extension           VARCHAR(20),
    id_documento        BIGINT       NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

-- ============================
-- 19. BITACORA
-- ============================
CREATE TABLE bitacora (
    id                    BIGSERIAL    PRIMARY KEY,
    tipo_accion           VARCHAR(50)  NOT NULL,
    fecha                 TIMESTAMP    NOT NULL,
    descripcion           TEXT,
    entidad_referenciada  VARCHAR(100),
    id_entidad_ref        BIGINT,
    id_usuario            BIGINT       NOT NULL REFERENCES usuarios(id),
    fecha_creacion        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion    TIMESTAMP
);
CREATE INDEX idx_bitacora_entidad ON bitacora(entidad_referenciada, id_entidad_ref);
CREATE INDEX idx_bitacora_fecha   ON bitacora(fecha);

-- ============================
-- 20. OBSERVACION
-- ============================
CREATE TABLE observaciones (
    id                    BIGSERIAL    PRIMARY KEY,
    descripcion           TEXT         NOT NULL,
    fecha                 TIMESTAMP    NOT NULL,
    estado                VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    entidad_referenciada  VARCHAR(100) NOT NULL,
    id_entidad_ref        BIGINT       NOT NULL,
    id_usuario            BIGINT       NOT NULL REFERENCES usuarios(id),
    fecha_creacion        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion    TIMESTAMP
);
CREATE INDEX idx_observacion_entidad ON observaciones(entidad_referenciada, id_entidad_ref);
