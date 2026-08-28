package com.benkih.estore.product.entity;

import com.benkih.estore.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(
    name = "product_attributes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_attribute",
        columnNames = {"product_id", "attribute_id"}
    )
)
public class ProductAttribute extends BaseEntity {

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