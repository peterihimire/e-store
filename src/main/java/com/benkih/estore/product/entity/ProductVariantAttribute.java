package com.benkih.estore.product.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.exceptions.BadRequestException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "product_variant_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_variant_attribute",
            columnNames = {
                "variant_id",
                "attribute_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class ProductVariantAttribute extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "variant_id",
      nullable = false
  )
  private ProductVariant variant;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "attribute_id",
      nullable = false
  )
  private CategoryAttribute attribute;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_value_id")
  private AttributeValue attributeValue;

  @Column(length = 255)
  private String customValue;

  public ProductVariantAttribute(
      ProductVariant variant,
      CategoryAttribute attribute,
      AttributeValue attributeValue
  ) {
    this.variant = variant;
    this.attribute = attribute;
    this.attributeValue = attributeValue;
  }

  public ProductVariantAttribute(
      ProductVariant variant,
      CategoryAttribute attribute,
      String customValue
  ) {
    this.variant = variant;
    this.attribute = attribute;
    this.customValue = customValue;
  }

  public void validate() {
    boolean hasValue = attributeValue != null;
    boolean hasCustomValue = customValue != null && !customValue.isBlank();

    if (hasValue == hasCustomValue) {
      throw new BadRequestException(
          "Provide either attributeValue or customValue, but not both."
      );
    }

    if (!attribute.isVariantAttribute()) {
      throw new BadRequestException(
          "This attribute cannot be used as a variant option."
      );
    }

    if (attributeValue != null
        && !attributeValue.getAttribute().getId().equals(attribute.getId())) {
      throw new BadRequestException(
          "Attribute value does not belong to the selected attribute."
      );
    }
  }
}
