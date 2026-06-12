package com.redmuqui.platform.territorio.entity;

import com.redmuqui.platform.common.catalog.entity.BaseCatalogo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "territorios")
@Getter
@Setter
@NoArgsConstructor
public class Territorio extends BaseCatalogo {

    /** Nivel del territorio. Hoy el catálogo base es de departamentos. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTerritorio tipo = TipoTerritorio.DEPARTAMENTO;

    /**
     * Código UBIGEO (INEI). Estable; cruza con la geometría del mapa territorial.
     * Nulo para territorios libres creados a mano.
     */
    @Column(length = 10, unique = true)
    private String codigo;

    /** Jerarquía territorial opcional (p. ej. provincia → departamento). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_padre")
    private Territorio padre;
}
