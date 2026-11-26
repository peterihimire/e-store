package com.benkih.estore.checkout.entity;

// package com.benkih.estore.shipping.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.product.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "shipping_rates",
        indexes = {
            @Index(
                    name = "idx_shipping_rates_lookup",
                    columnList = "zone, delivery_method, min_weight_kg, max_weight_kg")
        })
public class ShippingRate extends AuditableEntity {

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false, length = 50)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false, length = 50)
    private DeliveryMethod deliveryMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "min_weight_kg", precision = 10, scale = 2)
    private BigDecimal minWeightKg;

    @Column(name = "max_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fee;

    @Column(name = "free_shipping_threshold", precision = 18, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(nullable = false)
    private boolean active = true;
}
