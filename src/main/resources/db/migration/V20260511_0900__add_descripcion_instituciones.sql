-- =====================================================================
-- V20260511_0900__add_descripcion_instituciones.sql
-- Agrega columna descripcion a instituciones para alinear la entidad
-- con la jerarquía BaseCatalogo (nombre + descripcion).
-- =====================================================================

ALTER TABLE instituciones ADD COLUMN descripcion TEXT;
