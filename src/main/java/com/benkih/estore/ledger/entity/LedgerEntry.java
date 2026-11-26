package com.benkih.estore.ledger.entity;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.settlement.entity.Settlement;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
            @Index(name = "idx_ledger_entry_account", columnList = "account_id"),
            @Index(name = "idx_ledger_entry_business", columnList = "business_id"),
            @Index(name = "idx_ledger_entry_allocation", columnList = "allocation_id"),
            @Index(name = "idx_ledger_entry_settlement", columnList = "settlement_id"),
            @Index(name = "idx_ledger_entry_payout", columnList = "payout_id"),
            @Index(name = "idx_ledger_entry_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LedgerAccount account;

    /**
     * Seller/business associated with this entry.
     *
     * <p>Null is allowed for platform-level accounts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_id")
    private Allocation allocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id")
    private Payout payout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LedgerEntryDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(length = 500)
    private String description;
}
