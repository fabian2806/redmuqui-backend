package com.redmuqui.platform.proyecto.entity;

import com.redmuqui.platform.institucion.entity.Institucion;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tabla intermedia entre Proyecto e Institucion.
 * Es entidad explícita porque tiene atributo {@code tipoParticipacion}.
 */
@Entity
@Table(name = "proyecto_institucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProyectoInstitucion.PK.class)
public class ProyectoInstitucion {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_institucion")
    private Institucion institucion;

    @Column(name = "tipo_participacion", length = 100)
    private String tipoParticipacion;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class PK implements Serializable {
        private Long proyecto;
        private Long institucion;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(proyecto, pk.proyecto) && Objects.equals(institucion, pk.institucion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(proyecto, institucion);
        }
    }
}
