package com.redmuqui.platform.actividad.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subactividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subactividad extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    @Builder.Default
    private Double presupuesto = 0.0;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "porcentaje_avance", nullable = false)
    @Builder.Default
    private Integer porcentajeAvance = 0;

    @Column(name = "hombres_involucrados", columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer hombresInvolucrados = 0;

    @Column(name = "mujeres_involucradas", columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer mujeresInvolucradas = 0;

    @Column(name = "fecha_inicio_planificada")
    private LocalDate fechaInicioPlanificada;

    @Column(name = "fecha_fin_planificada")
    private LocalDate fechaFinPlanificada;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoSubactividad estado = EstadoSubactividad.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    @OneToMany(mappedBy = "subactividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubactividadArchivo> archivosEvidencia = new ArrayList<>();

    @OneToMany(mappedBy = "subactividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubactividadCofinanciamiento> cofinanciamientos = new ArrayList<>();

    public void addArchivo(SubactividadArchivo archivo) {
        archivosEvidencia.add(archivo);
        archivo.setSubactividad(this);
    }

    public void removeArchivo(SubactividadArchivo archivo) {
        archivosEvidencia.remove(archivo);
        archivo.setSubactividad(null);
    }

    public void addCofinanciamiento(SubactividadCofinanciamiento cofinanciamiento) {
        cofinanciamientos.add(cofinanciamiento);
        cofinanciamiento.setSubactividad(this);
    }

    public void removeCofinanciamiento(SubactividadCofinanciamiento cofinanciamiento) {
        cofinanciamientos.remove(cofinanciamiento);
        cofinanciamiento.setSubactividad(null);
    }
}
