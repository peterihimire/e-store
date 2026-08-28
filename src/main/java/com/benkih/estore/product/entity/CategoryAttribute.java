package com.benkih.estore.product.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.AttributeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "category_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_category_attribute",
            columnNames = {
                "category_id",
                "name"
            }
        )
    }
)
public class CategoryAttribute extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "category_id",
      nullable = false
  )
  private Category category;

  @NotBlank
  @Column(
      nullable = false,
      length = 100
  )
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(
      nullable = false,
      length = 30
  )
  private AttributeType type = AttributeType.TEXT;

  @Column(nullable = false)
  private boolean required = false;

  @Column(nullable = false)
  private boolean variantAttribute = false;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private Integer displayOrder = 0;

  @OneToMany(
      mappedBy = "attribute",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<AttributeValue> values = new ArrayList<>();

  public CategoryAttribute(
      Category category,
      String name,
      AttributeType type
  ) {
    this.category = category;
    this.name = name;
    this.type = type;
  }
}