package com.redmuqui.platform.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base para entidades que requieren auditoría automática de timestamps.
 * Las entidades hijas heredan los campos fechaCreacion y fechaModificacion,
 * que son gestionados automáticamente por Spring Data JPA.
 *
 * Esta auditoría es TÉCNICA (timestamps automáticos).
 * La auditoría de NEGOCIO (eventos consultables por el usuario) se maneja en la entidad Bitacora.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
