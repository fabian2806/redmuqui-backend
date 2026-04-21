package com.redmuqui.platform.ejetematico.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ejes_tematicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EjeTematico extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
