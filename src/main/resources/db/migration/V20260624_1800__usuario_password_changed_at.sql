ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS contrasenha_actualizada_en TIMESTAMP;
