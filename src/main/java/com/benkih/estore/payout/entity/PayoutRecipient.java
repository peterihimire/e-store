package com.benkih.estore.payout.entity;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.payout.enums.PayoutProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "payout_recipients",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_payout_recipient_bank_provider",
                    columnNames = {"bank_account_id", "provider"})
        },
        indexes = {
            @Index(name = "idx_payout_recipient_bank_account", columnList = "bank_account_id"),
            @Index(name = "idx_payout_recipient_provider", columnList = "provider"),
            @Index(name = "idx_payout_recipient_code", columnList = "recipient_code")
        })
@Getter
@Setter
@NoArgsConstructor
public class PayoutRecipient extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayoutProvider provider;

    @Column(name = "recipient_code", nullable = false, length = 100)
    private String recipientCode;

    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Column(nullable = false)
    private boolean active = true;
}
