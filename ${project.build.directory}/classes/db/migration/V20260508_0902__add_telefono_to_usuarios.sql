-- Agrega columna de teléfono para RF-012 (registro de usuarios)
-- Compatible con entornos donde la columna todavía no existe.
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS telefono VARCHAR(30);

ALTER TABLE usuarios
    DROP COLUMN IF EXISTS id_macroregion;
