package com.benkih.estore.ledger.enums;

public enum LedgerTransactionType {

  // =========================
  // PAYMENT
  // =========================

  PAYMENT_RECEIVED,

  PROCESSOR_FEE,


  // =========================
  // SELLER ALLOCATION
  // =========================

  ALLOCATION_POSTED,

  PLATFORM_FEE,


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
  SHIPPING_CHARGE,
  TAX_COLLECTED,
  PROCESSOR_FEE_RECOVERY
}

//public enum LedgerTransactionType {
//
//  PAYMENT_RECEIVED,
//
//  ALLOCATION_POSTED,
//
//  SETTLEMENT_RELEASE,
//
//  RESERVE_HOLD,
//  RESERVE_RELEASE,
//
//  REFUND,
//
//  PLATFORM_FEE,
//  PROCESSOR_FEE,
//
//  PAYOUT_INITIATED,
//  PAYOUT_COMPLETED,
//  PAYOUT_FAILED,
//
//  CHARGEBACK,
//
//  ADJUSTMENT,
//  REVERSAL
//}
