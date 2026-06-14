package com.redmuqui.platform.documento.entity;

import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documento_versiones", uniqueConstraints = {
    @UniqueConstraint(name = "uk_documento_version", columnNames = {"id_documento", "numero_version"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento", nullable = false)
    private Documento documento;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 100)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoDocumento estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_eje_tematico")
    private EjeTematico ejeTematico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resp_elaboracion", nullable = false)
    private Usuario responsableElaboracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resp_validacion")
    private Usuario responsableValidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cambio", nullable = false)
    private Usuario usuarioCambio;

    @Column(name = "motivo_cambio", length = 500)
    private String motivoCambio;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }
}
