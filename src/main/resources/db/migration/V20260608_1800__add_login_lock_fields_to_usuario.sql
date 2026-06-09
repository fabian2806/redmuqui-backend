ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS intentos_login_fallidos INTEGER;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS bloqueado_hasta TIMESTAMP WITHOUT TIME ZONE;

UPDATE usuarios
SET intentos_login_fallidos = 0
WHERE intentos_login_fallidos IS NULL;

ALTER TABLE usuarios
    ALTER COLUMN intentos_login_fallidos SET DEFAULT 0;

ALTER TABLE usuarios
    ALTER COLUMN intentos_login_fallidos SET NOT NULL;