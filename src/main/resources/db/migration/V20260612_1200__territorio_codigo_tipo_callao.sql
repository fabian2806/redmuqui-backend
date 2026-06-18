-- =====================================================================
-- V20260612_1200__territorio_codigo_tipo_callao.sql
-- Mapa Territorial (Sprint 4 ④).
-- Da a los territorios un nivel (tipo), un código UBIGEO estable y una
-- jerarquía opcional; completa el catálogo de departamentos con sus códigos
-- y agrega el Callao (faltaba en el seed inicial). Diseño escalable: mañana
-- se agregan provincias/distritos como filas con id_padre, sin tocar el esquema.
-- =====================================================================

-- 1) Nuevas columnas. tipo con default para que los 24 ya sembrados y cualquier
--    INSERT existente queden consistentes; codigo/id_padre nullable para no
--    romper la creación de territorios libres desde la UI.
ALTER TABLE territorios ADD COLUMN tipo     VARCHAR(30) NOT NULL DEFAULT 'DEPARTAMENTO';
ALTER TABLE territorios ADD COLUMN codigo   VARCHAR(10);
ALTER TABLE territorios ADD COLUMN id_padre BIGINT;

ALTER TABLE territorios
    ADD CONSTRAINT fk_territorio_padre FOREIGN KEY (id_padre) REFERENCES territorios (id);

-- 2) Backfill del UBIGEO de los 24 departamentos sembrados en V20260602_1000.
--    El código NO es alfabético (Callao=07 corre a Cusco a 08 en adelante).
UPDATE territorios t SET codigo = c.codigo
FROM (VALUES
    ('Amazonas','01'), ('Áncash','02'), ('Apurímac','03'), ('Arequipa','04'),
    ('Ayacucho','05'), ('Cajamarca','06'), ('Cusco','08'), ('Huancavelica','09'),
    ('Huánuco','10'), ('Ica','11'), ('Junín','12'), ('La Libertad','13'),
    ('Lambayeque','14'), ('Lima','15'), ('Loreto','16'), ('Madre de Dios','17'),
    ('Moquegua','18'), ('Pasco','19'), ('Piura','20'), ('Puno','21'),
    ('San Martín','22'), ('Tacna','23'), ('Tumbes','24'), ('Ucayali','25')
) AS c(nombre, codigo)
WHERE LOWER(t.nombre) = LOWER(c.nombre) AND t.codigo IS NULL;

-- 3) Callao (07): Provincia Constitucional, tratada como unidad de primer nivel
--    para el mapa. Idempotente, mismo patrón que el seed original.
INSERT INTO territorios (nombre, descripcion, tipo, codigo)
SELECT 'Callao',
       'Provincia Constitucional del Callao, usada como territorio de referencia para proyectos.',
       'DEPARTAMENTO', '07'
WHERE NOT EXISTS (
    SELECT 1 FROM territorios t WHERE LOWER(t.nombre) = LOWER('Callao')
);

-- 4) Unicidad del código (permite múltiples NULL en Postgres para territorios libres).
CREATE UNIQUE INDEX ux_territorios_codigo ON territorios (codigo);
