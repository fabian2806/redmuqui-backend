package com.redmuqui.platform.institucion.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instituciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institucion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String nombre;

    @Column(length = 100)
    private String tipo;
}
