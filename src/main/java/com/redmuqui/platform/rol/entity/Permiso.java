package com.redmuqui.platform.rol.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

/**
 * Acción atómica autorizable en el sistema.
 *
 * El campo {@code tipo} se usa para categorizar el permiso (LECTURA, ESCRITURA,
 * VALIDACION, ADMINISTRACION) según los RFs 017-019 que mencionan estas categorías.
 * Los valores exactos se cargan vía seed inicial (ver V8__seed_roles_permisos.sql cuando
 * se incorpore Flyway, o vía configuración inicial mientras tanto).
 */
@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 50)
    private String tipo;
}
