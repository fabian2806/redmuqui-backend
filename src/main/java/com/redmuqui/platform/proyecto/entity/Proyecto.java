package com.redmuqui.platform.proyecto.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.institucion.entity.Institucion;
import com.redmuqui.platform.macroregion.entity.Macroregion;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proyectos", indexes = {
    @Index(name = "idx_proyecto_codigo", columnList = "codigo_interno", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(name = "codigo_interno", nullable = false, unique = true, length = 50)
    private String codigoInterno;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "objetivo_general", columnDefinition = "TEXT")
    private String objetivoGeneral;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada", nullable = false)
    private LocalDate fechaFinEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoProyecto estado = EstadoProyecto.ACTIVO;

    @Column(name = "nivel_prioridad")
    private Integer nivelPrioridad;

    @Column(name = "porcentaje_avance")
    @Builder.Default
    private Double porcentajeAvance = 0.0;

    @Column(nullable = false)
    private Double presupuesto;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String moneda = "PEN";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "proyecto_macroregion",
        joinColumns = @JoinColumn(name = "id_proyecto"),
        inverseJoinColumns = @JoinColumn(name = "id_macroregion")
    )
    @Builder.Default
    private Set<Macroregion> macroregiones = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_eje_tematico")
    private EjeTematico ejeTematico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable_principal")
    private Usuario responsablePrincipal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "proyecto_territorio",
        joinColumns = @JoinColumn(name = "id_proyecto"),
        inverseJoinColumns = @JoinColumn(name = "id_territorio")
    )
    @Builder.Default
    private Set<Territorio> territorios = new HashSet<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProyectoEquipo> equipo = new HashSet<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProyectoInstitucion> instituciones = new HashSet<>();
}
