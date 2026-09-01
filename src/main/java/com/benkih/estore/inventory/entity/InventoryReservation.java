package com.benkih.estore.inventory.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "inventory_reservations",
    indexes = {
        @Index(
            name = "idx_inventory_reservation_checkout",
            columnList = "checkout_reference"
        ),
        @Index(
            name = "idx_inventory_reservation_expiry",
            columnList = "status, expires_at"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservation extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "inventory_id", nullable = false)
  private Inventory inventory;

  @Column(
      name = "checkout_reference",
      nullable = false,
      length = 100
  )
  private String checkoutReference;

  @Column(nullable = false)
  private Integer quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReservationStatus status;

  @Column(nullable = false)
  private Instant expiresAt;

}
