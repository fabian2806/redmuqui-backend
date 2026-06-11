package com.redmuqui.platform.actividad.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "actividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actividad extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoActividad estado = EstadoActividad.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hito")
    private Hito hito;

    @Column(name = "porcentaje_avance", columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer porcentajeAvance = 0;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subactividad> subactividades = new ArrayList<>();

    /**
     * Responsables de la actividad. Relación N:M con Usuario.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "actividad_responsable",
        joinColumns = @JoinColumn(name = "id_actividad"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    @Builder.Default
    private Set<Usuario> responsables = new HashSet<>();
}
