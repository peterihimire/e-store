package com.benkih.estore.checkout.entity;

//package com.benkih.estore.shipping.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "shipping_rates",
    indexes = {
        @Index(
            name = "idx_shipping_rates_lookup",
            columnList = "zone, delivery_method, min_weight_kg, max_weight_kg"
        )
    }
)
public class ShippingRate extends AuditableEntity {

  @Column(
      nullable = false,
      unique = true,
      updatable = false
  )
  private String code;

  @Column(
      nullable = false,
      length = 50
  )
  private String zone;

  @Column(
      name = "delivery_method",
      nullable = false,
      length = 50
  )
  private String deliveryMethod;

  @Column(
      name = "min_weight_kg",
      nullable = false,
      precision = 10,
      scale = 2
  )
  private BigDecimal minWeightKg;

  @Column(
      name = "max_weight_kg",
      precision = 10,
      scale = 2
  )
  private BigDecimal maxWeightKg;

  @Column(
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal fee;

  @Column(
      name = "free_shipping_threshold",
      precision = 18,
      scale = 2
  )
  private BigDecimal freeShippingThreshold;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode currency;

  @Column(name = "effective_from", nullable = false)
  private Instant effectiveFrom;

  @Column(name = "effective_to")
  private Instant effectiveTo;

  @Column(
      nullable = false
  )
  private boolean active = true;
}
