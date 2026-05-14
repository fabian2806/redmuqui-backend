package com.redmuqui.platform.territorio.entity;

import com.redmuqui.platform.common.catalog.entity.BaseCatalogo;
import jakarta.persistence.Entity;
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
}
