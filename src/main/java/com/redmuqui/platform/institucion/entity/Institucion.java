package com.redmuqui.platform.institucion.entity;

import com.redmuqui.platform.common.catalog.entity.BaseCatalogo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "instituciones")
@Getter
@Setter
public class Institucion extends BaseCatalogo {

    @Column(length = 100)
    private String tipo;
}
