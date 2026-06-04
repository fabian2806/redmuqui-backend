package com.redmuqui.platform.documento.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

/**
 * Archivo físico asociado a un Documento.
 * Permite mantener múltiples versiones / anexos de un mismo documento lógico.
 */
@Entity
@Table(name = "archivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archivo extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 20)
    private String extension;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tamanio_archivo", nullable = false)
    @Builder.Default
    private Long tamanioArchivo = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento", nullable = false)
    private Documento documento;
}
