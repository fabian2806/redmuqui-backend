CREATE TABLE configuracion_sistema (
                                       id BIGINT PRIMARY KEY,

                                       nombre_organizacion VARCHAR(150) NOT NULL,
                                       nombre_plataforma VARCHAR(150) NOT NULL,
                                       correo_soporte VARCHAR(150),
                                       telefono VARCHAR(50),
                                       direccion VARCHAR(255),
                                       sistema_activo BOOLEAN NOT NULL DEFAULT TRUE,

                                       tamanio_maximo_mb INTEGER NOT NULL DEFAULT 20,
                                       cantidad_maxima_adjuntos INTEGER NOT NULL DEFAULT 5,
                                       estado_inicial VARCHAR(50) NOT NULL DEFAULT 'Registrado',

                                       intentos_maximos_login INTEGER NOT NULL DEFAULT 5,
                                       tiempo_bloqueo_minutos INTEGER NOT NULL DEFAULT 15,
                                       duracion_access_token_minutos INTEGER NOT NULL DEFAULT 15,
                                       duracion_refresh_token_dias INTEGER NOT NULL DEFAULT 7,
                                       recuperacion_password BOOLEAN NOT NULL DEFAULT TRUE,
                                       cierre_por_inactividad BOOLEAN NOT NULL DEFAULT TRUE,

                                       creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO configuracion_sistema (
    id,
    nombre_organizacion,
    nombre_plataforma,
    correo_soporte,
    telefono,
    direccion,
    sistema_activo,
    tamanio_maximo_mb,
    cantidad_maxima_adjuntos,
    estado_inicial,
    intentos_maximos_login,
    tiempo_bloqueo_minutos,
    duracion_access_token_minutos,
    duracion_refresh_token_dias,
    recuperacion_password,
    cierre_por_inactividad
) VALUES (
             1,
             'Red Muqui',
             'Plataforma de Gestión Institucional',
             'contacto@muqui.org',
             '+51 1 234 5678',
             'Lima, Perú',
             TRUE,
             20,
             5,
             'Registrado',
             5,
             15,
             15,
             7,
             TRUE,
             TRUE
         );