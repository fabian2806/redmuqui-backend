package com.redmuqui.platform.macroregion.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "macroregiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Macroregion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
