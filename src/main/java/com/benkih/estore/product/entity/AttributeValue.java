package com.benkih.estore.product.entity;


import com.benkih.estore.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "attribute_values",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attribute_value",
            columnNames = {
                "attribute_id",
                "value"
            }
        )
    }
)
public class AttributeValue extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "attribute_id",
      nullable = false
  )
  private CategoryAttribute attribute;

  @NotBlank
  @Column(
      nullable = false,
      length = 100
  )
  private String value;

  @Column(length = 100)
  private String displayName;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private Integer displayOrder = 0;

  public AttributeValue(
      CategoryAttribute attribute,
      String value
  ) {
    this.attribute = attribute;
    this.value = value;
    this.displayName = value;
  }
}
