ALTER TABLE archivos
    ADD COLUMN IF NOT EXISTS descripcion TEXT,
    ADD COLUMN IF NOT EXISTS tamanio_archivo BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS documento_enlaces (
    id                  BIGSERIAL PRIMARY KEY,
    url                 VARCHAR(1000) NOT NULL,
    descripcion         TEXT NOT NULL,
    id_documento        BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_documento_enlaces_documento
    ON documento_enlaces(id_documento);
