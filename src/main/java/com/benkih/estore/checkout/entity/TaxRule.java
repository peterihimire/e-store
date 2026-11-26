package com.benkih.estore.checkout.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.Country;
import com.benkih.estore.common.enums.TaxCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
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
        name = "tax_rules",
        indexes = {
            @Index(
                    name = "idx_tax_rules_lookup",
                    columnList = "jurisdiction, tax_category, effective_from, effective_to")
        })
public class TaxRule extends AuditableEntity {

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Country country;

    @Column(nullable = false, length = 50)
    private String jurisdiction;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_category", nullable = false, length = 30)
    private TaxCategory taxCategory;

    @Column(nullable = false, precision = 8, scale = 5)
    private BigDecimal rate;

    @Column(name = "tax_type", nullable = false, length = 30)
    private String taxType;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(nullable = false)
    private boolean active = true;
}
