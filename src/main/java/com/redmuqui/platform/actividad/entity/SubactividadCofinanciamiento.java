package com.redmuqui.platform.actividad.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subactividad_cofinanciamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubactividadCofinanciamiento {

    @EmbeddedId
    private SubactividadCofinanciamientoId id = new SubactividadCofinanciamientoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSubactividad")
    @JoinColumn(name = "id_subactividad")
    private Subactividad subactividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idActividadOrigen")
    @JoinColumn(name = "id_actividad_origen")
    private Actividad actividadOrigen;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificacion;
}
