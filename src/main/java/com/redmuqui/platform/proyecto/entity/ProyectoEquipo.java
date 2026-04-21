package com.redmuqui.platform.proyecto.entity;

import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tabla intermedia entre Proyecto y Usuario.
 * Es entidad explícita (no @JoinTable simple) porque tiene atributo {@code rolEnProyecto}.
 *
 * NOTA: Por ahora rolEnProyecto es String descriptivo (etiqueta).
 * Si se decide que debe controlar permisos sobre el proyecto específico,
 * se reemplazará por una FK a una nueva entidad RolProyecto.
 */
@Entity
@Table(name = "proyecto_equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProyectoEquipo.PK.class)
public class ProyectoEquipo {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "rol_en_proyecto", length = 100)
    private String rolEnProyecto;

    /**
     * Clave primaria compuesta requerida por @IdClass.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class PK implements Serializable {
        private Long proyecto;
        private Long usuario;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(proyecto, pk.proyecto) && Objects.equals(usuario, pk.usuario);
        }

        @Override
        public int hashCode() {
            return Objects.hash(proyecto, usuario);
        }
    }
}
