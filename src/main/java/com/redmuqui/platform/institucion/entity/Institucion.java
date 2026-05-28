package com.redmuqui.platform.institucion.entity;

import com.redmuqui.platform.common.catalog.entity.BaseCatalogo;
import com.redmuqui.platform.proyecto.entity.ProyectoInstitucion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "instituciones")
@Getter
@Setter
public class Institucion extends BaseCatalogo {

    @Column(length = 100)
    private String tipo;

    @OneToMany(mappedBy = "institucion")
    private Set<ProyectoInstitucion> proyectos = new HashSet<>();
}
