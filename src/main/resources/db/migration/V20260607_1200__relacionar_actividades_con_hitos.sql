-- Relaciona cada actividad con el hito macro al que contribuye.
-- Se mantiene nullable para permitir regularizar actividades históricas existentes.
ALTER TABLE actividades
    ADD COLUMN id_hito BIGINT REFERENCES hitos(id) ON DELETE RESTRICT;

CREATE INDEX idx_actividades_id_hito ON actividades(id_hito);
