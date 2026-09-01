package com.benkih.estore.product.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "product_attributes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_attribute",
        columnNames = {"product_id", "attribute_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ProductAttribute extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "attribute_id", nullable = false)
  private CategoryAttribute attribute;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_value_id")
  private AttributeValue attributeValue;

  @Column(length = 255)
  private String customValue;
}