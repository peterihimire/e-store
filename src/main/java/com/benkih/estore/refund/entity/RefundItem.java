package com.benkih.estore.refund.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "refund_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_refund_item",
            columnNames = {"refund_id", "order_item_id"}
        )
    })
@Getter
@Setter
@NoArgsConstructor
public class RefundItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "refund_id", nullable = false)
  private Refund refund;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_item_id", nullable = false)
  private OrderItem orderItem;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;
}
