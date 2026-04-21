package com.redmuqui.platform.actividad.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "hitos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hito extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_clave", nullable = false)
    private LocalDate fechaClave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoHito estado = EstadoHito.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;
}
