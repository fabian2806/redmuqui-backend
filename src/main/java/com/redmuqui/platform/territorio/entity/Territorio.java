package com.redmuqui.platform.territorio.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "territorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Territorio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
