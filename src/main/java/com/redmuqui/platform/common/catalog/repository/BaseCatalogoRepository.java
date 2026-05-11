package com.redmuqui.platform.common.catalog.repository;

import com.redmuqui.platform.common.catalog.entity.BaseCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseCatalogoRepository<T extends BaseCatalogo> extends JpaRepository<T, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
