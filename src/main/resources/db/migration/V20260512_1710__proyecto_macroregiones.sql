-- Permite asociar un proyecto a una o varias macroregiones.
-- Se preserva proyectos.id_macroregion como columna legacy para no perder datos
-- ni romper bases locales ya creadas; la relacion nueva vive en esta tabla N:M.

CREATE TABLE proyecto_macroregion (
    id_proyecto     BIGINT NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    id_macroregion  BIGINT NOT NULL REFERENCES macroregiones(id),
    PRIMARY KEY (id_proyecto, id_macroregion)
);

INSERT INTO proyecto_macroregion (id_proyecto, id_macroregion)
SELECT id, id_macroregion
FROM proyectos
WHERE id_macroregion IS NOT NULL
ON CONFLICT DO NOTHING;
