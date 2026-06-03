package com.redmuqui.platform.actividad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubactividadCofinanciamientoId implements Serializable {

    @Column(name = "id_subactividad")
    private Long idSubactividad;

    @Column(name = "id_actividad_origen")
    private Long idActividadOrigen;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubactividadCofinanciamientoId that = (SubactividadCofinanciamientoId) o;
        return Objects.equals(idSubactividad, that.idSubactividad) &&
               Objects.equals(idActividadOrigen, that.idActividadOrigen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSubactividad, idActividadOrigen);
    }
}
