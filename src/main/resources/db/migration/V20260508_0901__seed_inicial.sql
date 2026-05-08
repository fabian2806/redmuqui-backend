-- =====================================================================
-- V20260508_0901__seed_inicial.sql
-- Datos iniciales: roles, permisos, matriz rol-permiso, admin y catálogos.
--
-- IMPORTANTE: Cualquier modificación a este seed después de mergeado
-- debe hacerse en una migración nueva (no editar este archivo).
-- =====================================================================

-- ============================
-- ROLES (4 roles según DAS v1.0, sección 6.1)
-- ============================
INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Administrador del sistema. Acceso completo a todos los módulos.'),
    ('TECNICO',       'Equipo técnico. Crea y actualiza proyectos, actividades y documentos.'),
    ('COORDINADOR',   'Coordinación macroregional. Gestiona proyectos y valida documentos de su ámbito.'),
    ('CONSULTOR',     'Acceso de solo consulta a la plataforma.');

-- ============================
-- PERMISOS (alineados con módulos de Sprint 1; se amplían en sprints siguientes)
-- ============================
INSERT INTO permisos (nombre, tipo) VALUES
    -- Usuarios
    ('USUARIOS_READ',      'LECTURA'),
    ('USUARIOS_CREATE',    'ESCRITURA'),
    ('USUARIOS_UPDATE',    'ESCRITURA'),
    ('USUARIOS_DEACTIVATE','ADMINISTRACION'),
    -- Catálogos
    ('CATALOGOS_READ',     'LECTURA'),
    ('CATALOGOS_MANAGE',   'ADMINISTRACION'),
    -- Proyectos
    ('PROYECTOS_READ',     'LECTURA'),
    ('PROYECTOS_CREATE',   'ESCRITURA'),
    ('PROYECTOS_UPDATE',   'ESCRITURA'),
    -- Documentos (Sprint 3)
    ('DOCUMENTOS_READ',    'LECTURA'),
    ('DOCUMENTOS_CREATE',  'ESCRITURA'),
    ('DOCUMENTOS_UPDATE',  'ESCRITURA'),
    ('DOCUMENTOS_VALIDATE','VALIDACION'),
    -- Bitácora y reportes (Sprint 3-4)
    ('BITACORA_READ',      'LECTURA'),
    ('REPORTES_READ',      'LECTURA'),
    ('REPORTES_EXPORT',    'LECTURA');

-- ============================
-- MATRIZ ROL × PERMISO
-- ============================
-- ADMINISTRADOR: todos los permisos
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id, p.id
FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR';

-- TECNICO: lecturas globales + escritura en proyectos y documentos
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id, p.id
FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'TECNICO'
  AND p.nombre IN (
    'CATALOGOS_READ',
    'PROYECTOS_READ', 'PROYECTOS_CREATE', 'PROYECTOS_UPDATE',
    'DOCUMENTOS_READ', 'DOCUMENTOS_CREATE', 'DOCUMENTOS_UPDATE',
    'BITACORA_READ', 'REPORTES_READ'
  );

-- COORDINADOR: lecturas globales + escritura proyectos/documentos + validación + exportar
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id, p.id
FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'COORDINADOR'
  AND p.nombre IN (
    'USUARIOS_READ',
    'CATALOGOS_READ',
    'PROYECTOS_READ', 'PROYECTOS_CREATE', 'PROYECTOS_UPDATE',
    'DOCUMENTOS_READ', 'DOCUMENTOS_CREATE', 'DOCUMENTOS_UPDATE', 'DOCUMENTOS_VALIDATE',
    'BITACORA_READ', 'REPORTES_READ', 'REPORTES_EXPORT'
  );

-- CONSULTOR: solo lecturas
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id, p.id
FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'CONSULTOR'
  AND p.nombre IN (
    'CATALOGOS_READ',
    'PROYECTOS_READ',
    'DOCUMENTOS_READ',
    'REPORTES_READ'
  );

-- ============================
-- CATÁLOGO: MACROREGIONES (3 oficiales de Red Muqui)
-- ============================
INSERT INTO macroregiones (nombre, descripcion) VALUES
    ('Norte',  'Macroregión Norte (Cajamarca, Piura, La Libertad, Lambayeque, Tumbes, Áncash)'),
    ('Centro', 'Macroregión Centro (Lima, Junín, Pasco, Huancavelica, Huánuco, Ayacucho)'),
    ('Sur',    'Macroregión Sur (Arequipa, Apurímac, Cusco, Moquegua, Tacna, Puno)');

-- ============================
-- CATÁLOGO: EJES TEMÁTICOS (6 ejes oficiales)
-- ============================
INSERT INTO ejes_tematicos (nombre, descripcion) VALUES
    ('Agua y Territorio',             'Defensa del agua y los territorios frente a la actividad minera.'),
    ('Derechos Humanos',              'Defensa de derechos humanos de comunidades afectadas por minería.'),
    ('Minería Artesanal (MAPE)',      'Pequeña minería y minería artesanal: formalización y prácticas sostenibles.'),
    ('Vigilancia Ambiental',          'Monitoreo y vigilancia ambiental participativa.'),
    ('Incidencia Política',           'Incidencia ante autoridades nacionales e internacionales.'),
    ('Fortalecimiento Organizacional','Fortalecimiento de capacidades organizativas e institucionales.');

-- ============================
-- CATÁLOGO: INSTITUCIONES MIEMBRO (12 oficiales de Red Muqui)
-- ============================
INSERT INTO instituciones (nombre, tipo) VALUES
    ('CooperAcción',                    'ONG'),
    ('GRUFIDES',                        'ONG'),
    ('CEPES',                           'Centro de Estudios'),
    ('Fedepaz',                         'ONG'),
    ('Labor Pasco',                     'ONG'),
    ('CEDHA',                           'ONG'),
    ('Propuesta Ciudadana',             'Red'),
    ('CBC',                             'Centro de Estudios'),
    ('APRODEH',                         'Asociación'),
    ('Natura - Instituto Ambientalista','Instituto'),
    ('AMAS',                            'Asociación'),
    ('Proyecto Amigo',                  'Asociación');

-- ============================
-- USUARIO ADMIN INICIAL
-- email: admin@redmuqui.org
-- password: Admin123!  (BCrypt cost 12)
-- ⚠️ CAMBIAR EN PRIMER LOGIN antes de pasar a staging
-- ============================
INSERT INTO usuarios (
    nombres, apellidos, email, contrasenha_hash, estado, id_rol
) VALUES (
    'Administrador',
    'Sistema',
    'admin@redmuqui.org',
    '$2b$12$R21mUl7UPoNvXcWMLO9CFeiQCTPk3XhgmamEUuLKLBG/m7VSapere',
    TRUE,
    (SELECT id FROM roles WHERE nombre = 'ADMINISTRADOR')
);
