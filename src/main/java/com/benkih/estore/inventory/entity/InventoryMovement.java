package com.benkih.estore.inventory.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.InventoryMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "inventory_movements",
        indexes = {
            @Index(
                    name = "idx_inventory_movements_inventory",
                    columnList = "inventory_id, created_at"),
            @Index(
                    name = "idx_inventory_movements_reference",
                    columnList = "reference_type, reference_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovement extends AuditableEntity {

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
}
