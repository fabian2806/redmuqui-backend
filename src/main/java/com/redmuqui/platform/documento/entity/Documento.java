package com.redmuqui.platform.documento.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.ejetematico.entity.EjeTematico;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.territorio.entity.Territorio;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Documento institucional (informe, pronunciamiento, investigación, etc.).
 *
 * Es la entidad LÓGICA del documento. Los archivos físicos (PDF, DOCX) se modelan
 * en la entidad Archivo, lo que permite versionado: un Documento puede tener
 * múltiples Archivos (versiones / anexos).
 */
@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 100)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoDocumento estado = EstadoDocumento.BORRADOR;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;

    @Column(name = "fecha_carga", nullable = false)
    @Builder.Default
    private LocalDate fechaCarga = LocalDate.now();

    @Column(length = 500)
    private String enlace;

    @Builder.Default
    private Double version = 1.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_carga", nullable = false)
    private Usuario usuarioCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subactividad")
    private Subactividad subactividad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculo", nullable = false, length = 30)
    @Builder.Default
    private TipoVinculoDocumento tipoVinculo = TipoVinculoDocumento.GENERAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_eje_tematico")
    private EjeTematico ejeTematico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resp_elaboracion", nullable = false)
    private Usuario respElaboracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resp_validacion")
    private Usuario respValidacion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "documento_territorio",
        joinColumns = @JoinColumn(name = "id_documento"),
        inverseJoinColumns = @JoinColumn(name = "id_territorio")
    )
    @Builder.Default
    private Set<Territorio> territorios = new HashSet<>();

    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Archivo> archivos = new HashSet<>();
}
