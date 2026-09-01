package com.benkih.estore.order.entity;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.TaxCategory;
import com.benkih.estore.product.entity.Brand;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

  private int quantity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id")
  private ProductVariant variant;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String sku;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id")
  private Brand brand;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode currency;

  @Enumerated(EnumType.STRING)
  @Column(name = "tax_category", nullable = false)
  private TaxCategory taxCategory;

  @Column(name = "tax_rate", nullable = false, precision = 8, scale = 5)
  private BigDecimal taxRate;

  @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal subtotal;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  public OrderItem(int quantity, BigDecimal price, String name,
                   String sku, Brand brand, CurrencyCode currency, TaxCategory taxCategory, Order order,
                   Product product, Business business) {
    this.quantity = quantity;
    this.price = price;
    this.name = name;
    this.sku = sku;
    this.brand = brand;
    this.currency = currency;
    this.taxCategory = taxCategory;
    this.order = order;
    this.product = product;
    this.business = business;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @OneToMany(
      mappedBy = "orderItem",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Allocation> allocations = new ArrayList<>();

}
