package com.benkih.estore.cart.entity;

import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "cart_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cart_variant",
        columnNames = {"cart_id", "variant_id"}
    ))
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @NotNull
  @Min(1)
  @Column(nullable = false)
  private Integer quantity;

  @NotNull
  @DecimalMin(value = "0.00", inclusive = true)
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @NotNull
  @DecimalMin(value = "0.00", inclusive = true)
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPrice;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // @JsonIgnore     - using CartItemResponseDto fixed the circular injection
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

//  @ManyToOne
//  @JoinColumn(name = "product_id")
//  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "variant_id", nullable = false)
  private ProductVariant variant;

  public void setTotalPrice(){
    this.totalPrice = this.unitPrice.multiply(new BigDecimal(quantity));
  }

  public int getQuantity() {
    return quantity;
  } // made mistake initially by having quatity, so the getQuantity() method was never defined

  public void changeQuantity(int quantity) {
    if (quantity <= 0) {
      throw new BadRequestException("Quantity must be greater than zero.");
    }

    this.quantity = quantity;
    recalculateTotal();
  }

  public void updateUnitPrice(BigDecimal unitPrice) {
    if (unitPrice == null || unitPrice.signum() < 0) {
      throw new BadRequestException("Unit price cannot be negative.");
    }

    this.unitPrice = unitPrice;
    recalculateTotal();
  }

  private void recalculateTotal() {
    this.totalPrice = unitPrice
        .multiply(BigDecimal.valueOf(quantity))
        .setScale(2, RoundingMode.HALF_UP);
  }

  @PrePersist
  public void onCreate() {
    if (this.slug == null) {
      this.slug = UUID.randomUUID().toString();
    }
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

}
