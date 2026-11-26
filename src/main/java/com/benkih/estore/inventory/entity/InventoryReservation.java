package com.benkih.estore.inventory.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "inventory_reservations",
        indexes = {
            @Index(name = "idx_inventory_reservation_checkout", columnList = "checkout_reference"),
            @Index(name = "idx_inventory_reservation_expiry", columnList = "status, expires_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "checkout_reference", nullable = false, length = 100)
    private String checkoutReference;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant expiresAt;
}
