-- =====================================================================
-- V20260602_1000__seed_territorios_departamentos_peru.sql
-- Carga el catálogo base de territorios como departamentos del Perú.
-- =====================================================================

INSERT INTO territorios (nombre, descripcion)
SELECT data.nombre, data.descripcion
FROM (VALUES
    ('Amazonas',       'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Áncash',         'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Apurímac',       'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Arequipa',       'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Ayacucho',       'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Cajamarca',      'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Cusco',          'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Huancavelica',   'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Huánuco',        'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Ica',            'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Junín',          'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('La Libertad',    'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Lambayeque',     'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Lima',           'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Loreto',         'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Madre de Dios',  'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Moquegua',       'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Pasco',          'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Piura',          'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Puno',           'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('San Martín',     'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Tacna',          'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Tumbes',         'Departamento del Perú usado como territorio de referencia para proyectos.'),
    ('Ucayali',        'Departamento del Perú usado como territorio de referencia para proyectos.')
) AS data(nombre, descripcion)
WHERE NOT EXISTS (
    SELECT 1
    FROM territorios t
    WHERE LOWER(t.nombre) = LOWER(data.nombre)
);
