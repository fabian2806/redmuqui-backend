package com.redmuqui.platform.actividad.entity;

import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cronograma_reprogramaciones", indexes = {
    @Index(name = "idx_cronograma_reprogramacion_entidad", columnList = "tipo_entidad,id_entidad,fecha_creacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaReprogramacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad", nullable = false, length = 30)
    private TipoEntidadCronograma tipoEntidad;

    @Column(name = "id_entidad", nullable = false)
    private Long idEntidad;

    @Column(name = "fecha_inicio_anterior")
    private LocalDate fechaInicioAnterior;

    @Column(name = "fecha_fin_anterior")
    private LocalDate fechaFinAnterior;

    @Column(name = "fecha_inicio_nueva")
    private LocalDate fechaInicioNueva;

    @Column(name = "fecha_fin_nueva")
    private LocalDate fechaFinNueva;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
