package com.benkih.estore.inventory.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.exceptions.InsufficientReservedStockException;
import com.benkih.estore.common.exceptions.InsufficientStockException;
import com.benkih.estore.common.exceptions.InvalidInventoryQuantityException;
import com.benkih.estore.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventories")
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @Column(nullable = false)
  private Integer totalStock = 0;

  @Column(nullable = false)
  private Integer reservedStock = 0;

  @Column(nullable = false)
  private Integer damagedStock = 0;

  @Column(nullable = false)
  private Integer reorderLevel = 10;

  @Column(nullable = false)
  private Integer reorderQuantity = 50;

  @Version// for locking
  private Long version;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @OneToOne(fetch = FetchType.LAZY,optional = false)// optional false means
  // mandatory
  @JoinColumn(name = "product_id",nullable = false, unique = true) // the owning side,
  private Product product; // loading an inventory does not automatically
  // load the products

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  public int getAvailableStock() {
    return totalStock - reservedStock - damagedStock;
  }

  public void reserve(int quantity) {
    if (quantity <= 0) {
      throw new InvalidInventoryQuantityException(
          "Quantity must be greater than zero."
      );
    }

    if (getAvailableStock() < quantity) {
      throw new InsufficientStockException(
          "Only " + getAvailableStock() + " units are available."
      );
    }

    this.reservedStock += quantity;
  }

  public void release(int quantity) {
    if (quantity <= 0) {
      throw new InvalidInventoryQuantityException(
          "Quantity must be greater than zero."
      );
    }

    if (reservedStock < quantity) {
      throw new InsufficientReservedStockException(
          "Only " + reservedStock + " reserved units are available."
      );
    }

    this.reservedStock -= quantity;
  }

  public void markDamage(int quantity) {
    if (quantity <= 0) {
      throw new InvalidInventoryQuantityException(
          "Quantity must be greater than zero."
      );
    }

    if (getAvailableStock() < quantity) {
      throw new InsufficientStockException(
          "Only " + getAvailableStock() + " units are available."
      );
    }

    this.damagedStock += quantity;
  }

  public void addStock(int quantity) {
    if (quantity <= 0) {
      throw new InvalidInventoryQuantityException(
          "Quantity must be greater than zero."
      );
    }

    this.totalStock += quantity;
  }

  public void fulfillReservation(int quantity) {
    if (quantity <= 0) {
      throw new InvalidInventoryQuantityException(
          "Quantity must be greater than zero."
      );
    }

    if (reservedStock < quantity) {
      throw new InsufficientReservedStockException(
          "Only " + reservedStock + " reserved units are available."
      );
    }

    this.reservedStock -= quantity;
    this.totalStock -= quantity;
  }

  public boolean needsReorder() {
    return getAvailableStock() <= reorderLevel;
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
