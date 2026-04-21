package com.redmuqui.platform.trazabilidad.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro automático de eventos del sistema (RF-062, RF-063, RF-066).
 *
 * Usa patrón polimórfico: {@code entidadReferenciada} (nombre de la entidad)
 * + {@code idEntidadRef} (ID del registro afectado). Esto permite registrar eventos
 * sobre cualquier entidad sin crear tablas de bitácora por cada una.
 *
 * Trade-off: pierde integridad referencial a nivel de BD, pero gana flexibilidad.
 */
@Entity
@Table(name = "bitacora", indexes = {
    @Index(name = "idx_bitacora_entidad", columnList = "entidad_referenciada,id_entidad_ref"),
    @Index(name = "idx_bitacora_fecha", columnList = "fecha")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bitacora extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_accion", nullable = false, length = 50)
    private String tipoAccion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "entidad_referenciada", length = 100)
    private String entidadReferenciada;

    @Column(name = "id_entidad_ref")
    private Long idEntidadRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
