package com.benkih.estore.ledger.enums;

public enum LedgerTransactionType {

  // =========================
  // PAYMENT
  // =========================
  PAYMENT_RECEIVED,
  PROCESSOR_FEE, //  the actual fee Paystack charges Benkih.
  PROCESSOR_FEE_RECOVERY, //  Benkih recovers that fee from the seller’s entitlement.
  SHIPPING_CHARGE, //  customer-paid shipping.
  TAX_COLLECTED, //  tax component collected from customer.

  // =========================
  // SELLER ALLOCATION
  // =========================
  ALLOCATION_POSTED,
  PLATFORM_FEE, //  Benkih’s marketplace commission.

  // =========================
  // SETTLEMENT
  // =========================
  SETTLEMENT_RELEASE,

  // =========================
  // SELLER RESERVE
  // =========================
  RESERVE_HOLD,
  RESERVE_RELEASE,

  // =========================
  // REFUNDS / CHARGEBACKS
  // =========================
  REFUND,
  CHARGEBACK,

  // =========================
  // PAYOUT
  // =========================
  PAYOUT_INITIATED,
  PAYOUT_COMPLETED,
  PAYOUT_FAILED,

  // =========================
  // CORRECTIONS
  // =========================
  ADJUSTMENT,
  REVERSAL,
}