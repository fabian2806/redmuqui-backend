package com.redmuqui.platform.estado.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "estados",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nombre", "modulo"}, name = "uk_estado_nombre_modulo")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estado extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String codigo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ModuloEstado modulo;

    @Column(nullable = false)
    private Boolean activo = true;
}
