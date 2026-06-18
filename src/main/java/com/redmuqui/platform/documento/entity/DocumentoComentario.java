package com.redmuqui.platform.documento.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documento_comentarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoComentario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento", nullable = false)
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comentario;
}
