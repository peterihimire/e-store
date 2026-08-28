package com.benkih.estore.inventory.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.InventoryMovementType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "inventory_movements",
    indexes = {
        @Index(
            name = "idx_inventory_movements_inventory",
            columnList = "inventory_id, created_at"
        ),
        @Index(
            name = "idx_inventory_movements_reference",
            columnList = "reference_type, reference_id"
        )
    }
)
public class InventoryMovement extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "inventory_id", nullable = false)
  private Inventory inventory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private InventoryMovementType type;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "reference_type", length = 50)
  private String referenceType;

  @Column(name = "reference_id", length = 100)
  private String referenceId;

  @Column(length = 500)
  private String reason;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
