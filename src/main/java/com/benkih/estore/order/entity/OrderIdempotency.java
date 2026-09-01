package com.benkih.estore.order.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "order_idempotency",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_order_idempotency_user_key",
            columnNames = {"user_id", "idempotency_key"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class OrderIdempotency extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(
      name = "idempotency_key",
      nullable = false,
      length = 100
  )
  private String idempotencyKey;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;
}