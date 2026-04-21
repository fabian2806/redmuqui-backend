package com.redmuqui.platform.trazabilidad.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Observación o incidencia registrada manualmente por un usuario sobre una entidad
 * del sistema (proyecto, actividad o documento) según RF-067, RF-068.
 *
 * Usa el mismo patrón polimórfico que Bitacora.
 */
@Entity
@Table(name = "observaciones", indexes = {
    @Index(name = "idx_observacion_entidad", columnList = "entidad_referenciada,id_entidad_ref")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Observacion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoObservacion estado = EstadoObservacion.PENDIENTE;

    @Column(name = "entidad_referenciada", nullable = false, length = 100)
    private String entidadReferenciada;

    @Column(name = "id_entidad_ref", nullable = false)
    private Long idEntidadRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
