package com.benkih.estore.inventory.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.exceptions.InsufficientReservedStockException;
import com.benkih.estore.common.exceptions.InsufficientStockException;
import com.benkih.estore.common.exceptions.InvalidInventoryQuantityException;
import com.benkih.estore.product.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventories")
public class Inventory extends AuditableEntity {

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

    @Version // for locking
    private Long version;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false, unique = true)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    public int getAvailableStock() {
        return totalStock - reservedStock - damagedStock;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryQuantityException("Quantity must be greater than zero.");
        }

        if (getAvailableStock() < quantity) {
            throw new InsufficientStockException(
                    "Only " + getAvailableStock() + " units are available.");
        }

        this.reservedStock += quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryQuantityException("Quantity must be greater than zero.");
        }

        if (reservedStock < quantity) {
            throw new InsufficientReservedStockException(
                    "Only " + reservedStock + " reserved units are available.");
        }

        this.reservedStock -= quantity;
    }

    public void markDamage(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryQuantityException("Quantity must be greater than zero.");
        }

        if (getAvailableStock() < quantity) {
            throw new InsufficientStockException(
                    "Only " + getAvailableStock() + " units are available.");
        }

        this.damagedStock += quantity;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryQuantityException("Quantity must be greater than zero.");
        }

        this.totalStock += quantity;
    }

    public void fulfillReservation(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryQuantityException("Quantity must be greater than zero.");
        }

        if (reservedStock < quantity) {
            throw new InsufficientReservedStockException(
                    "Only " + reservedStock + " reserved units are available.");
        }

        this.reservedStock -= quantity;
        this.totalStock -= quantity;
    }

    public boolean needsReorder() {
        return getAvailableStock() <= reorderLevel;
    }
}
