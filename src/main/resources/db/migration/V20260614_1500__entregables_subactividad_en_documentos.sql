-- Unifica las nuevas evidencias de subactividad con el módulo formal de Documentos.
-- Los registros históricos de subactividad_archivos se conservan sin cambios.
ALTER TABLE documentos
    ADD COLUMN id_subactividad BIGINT REFERENCES subactividades(id) ON DELETE SET NULL,
    ADD COLUMN tipo_vinculo VARCHAR(30) NOT NULL DEFAULT 'GENERAL';

CREATE INDEX idx_documentos_subactividad
    ON documentos(id_subactividad);

CREATE UNIQUE INDEX uk_documento_entregable_final_subactividad
    ON documentos(id_subactividad)
    WHERE id_subactividad IS NOT NULL
      AND tipo_vinculo = 'ENTREGABLE_FINAL';
