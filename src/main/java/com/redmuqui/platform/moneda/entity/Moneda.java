package com.redmuqui.platform.moneda.entity;

import com.redmuqui.platform.common.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monedas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Moneda extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String nombre;

    @Column(nullable = false, unique = true, length = 3)
    private String codigo;

    @Column(nullable = false, length = 10)
    private String simbolo;

    @Column(nullable = false)
    private Boolean activo = true;
}
