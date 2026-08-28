package com.benkih.estore.product.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.inventory.entity.Inventory;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_variant_business_sku",
            columnNames = {"business_id", "sku"}
        ),
        @UniqueConstraint(
            name = "uk_variant_product_combination",
            columnNames = {"product_id", "combination_key"}
        )
    }
)
public class ProductVariant  extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @OneToMany(
      mappedBy = "variant",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<ProductVariantAttribute> attributes = new ArrayList<>();

  @Column(nullable = false, length = 100)
  private String sku;

  @NotNull
  @DecimalMin(
      value = "0.00",
      inclusive = true
  )
  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode currency = CurrencyCode.NGN;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "combination_key", nullable = false, length = 500)
  private String combinationKey;

  @OneToOne(
      mappedBy = "variant",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY
  )
  private Inventory inventory;

  public ProductVariant(
      Product product,
      Business business,
      String sku,
      BigDecimal price,
      CurrencyCode currency
  ) {
    this.product = product;
    this.business = business;
    this.sku = sku;
    this.price = price;
    this.currency = currency;
  }
}
