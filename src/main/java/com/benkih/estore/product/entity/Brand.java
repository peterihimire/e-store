package com.benkih.estore.product.entity;

import com.benkih.estore.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "brands",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_brand_normalized_name",
        columnNames = "normalized_name"
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Brand extends BaseEntity {

  @Column(nullable = false, length = 150)
  private String name;

  @Column(name = "normalized_name", nullable = false, length = 150)
  private String normalizedName;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private boolean verified = false;

  private String logoUrl;
  private String websiteUrl;
}
