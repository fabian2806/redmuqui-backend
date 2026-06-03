package com.redmuqui.platform.proyecto.specification;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Especificaciones JPA para filtrado de proyectos.
 * Centraliza todos los predicados para evitar duplicación y facilitar
 * su composición en el servicio.
 *
 * RF-082 Búsqueda general         (q)
 * RF-083 Búsqueda avanzada        (q + combinación de filtros)
 * RF-084 Filtrar por estado       (estado)
 * RF-085 Filtrar por macroregión  (idMacroregion)
 * RF-086 Filtrar por eje temático (idEjeTematico)
 * RF-087 Filtrar por institución  (idInstitucion)
 * Año de inicio                   (anio)
 */
public final class ProyectoSpecification {

    private ProyectoSpecification() {}

    /**
     * Construye la Specification combinada a partir de todos los parámetros
     * de búsqueda disponibles. Los parámetros nulos o en blanco son ignorados.
     */
    public static Specification<Proyecto> construir(
        String q,
        EstadoProyecto estado,
        Long idMacroregion,
        Long idEjeTematico,
        Long idInstitucion,
        Integer anio
    ) {
        Specification<Proyecto> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("codigoInterno")), patron)
            ));
        }

        if (estado != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("estado"), estado)
            );
        }

        if (idMacroregion != null) {
            spec = spec.and((root, query, cb) -> {
                if (Long.class != query.getResultType()) {
                    query.distinct(true);
                }
                return cb.equal(
                    root.join("macroregiones", JoinType.LEFT).get("id"),
                    idMacroregion
                );
            });
        }

        if (idEjeTematico != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("ejeTematico").get("id"), idEjeTematico)
            );
        }

        if (idInstitucion != null) {
            spec = spec.and((root, query, cb) -> {
                if (Long.class != query.getResultType()) {
                    query.distinct(true);
                }
                return cb.equal(
                    root.join("instituciones", JoinType.LEFT)
                        .get("institucion").get("id"),
                    idInstitucion
                );
            });
        }

        if (anio != null) {
            LocalDate inicio = LocalDate.of(anio, 1, 1);
            LocalDate fin    = LocalDate.of(anio, 12, 31);
            spec = spec.and((root, query, cb) ->
                cb.between(root.get("fechaInicio"), inicio, fin)
            );
        }

        return spec;
    }
}
