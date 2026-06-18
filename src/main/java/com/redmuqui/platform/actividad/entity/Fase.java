package com.redmuqui.platform.actividad.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fase extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio_planificada", nullable = false)
    private LocalDate fechaInicioPlanificada;

    @Column(name = "fecha_fin_planificada", nullable = false)
    private LocalDate fechaFinPlanificada;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoFase estado = EstadoFase.PENDIENTE;

    @Column(name = "porcentaje_avance", nullable = false)
    @Builder.Default
    private Double porcentajeAvance = 0D;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;
}
