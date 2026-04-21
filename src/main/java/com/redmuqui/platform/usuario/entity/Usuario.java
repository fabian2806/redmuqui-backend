package com.redmuqui.platform.usuario.entity;

import com.redmuqui.platform.common.audit.Auditable;
import com.redmuqui.platform.institucion.entity.Institucion;
import com.redmuqui.platform.macroregion.entity.Macroregion;
import com.redmuqui.platform.rol.entity.Rol;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "contrasenha_hash", nullable = false)
    private String contrasenhaHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_macroregion")
    private Macroregion macroregion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_institucion")
    private Institucion institucion;

    @Column(name = "ultimo_acceso")
    private java.time.LocalDateTime ultimoAcceso;
}
