package com.redmuqui.platform.documento.specification;

import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class DocumentoSpecification {
    private DocumentoSpecification() {}

    public static Specification<Documento> construir(
        String q,
        Long idProyecto,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        EstadoDocumento estado
    ) {
        Specification<Documento> spec = Specification.where(null);
        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titulo")), patron),
                cb.like(cb.lower(root.get("descripcion")), patron),
                cb.like(cb.lower(root.get("tipo")), patron)
            ));
        }
        if (idProyecto != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("proyecto").get("id"), idProyecto));
        }
        if (fechaDesde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaCarga"), fechaDesde));
        }
        if (fechaHasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaCarga"), fechaHasta));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return spec;
    }
}
