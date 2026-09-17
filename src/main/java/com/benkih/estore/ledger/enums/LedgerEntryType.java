package com.benkih.estore.ledger.enums;


public enum LedgerEntryType {

  // Seller liabilities
  SELLER_PENDING,
  SELLER_AVAILABLE,
  SELLER_RESERVED,

  // Payout liability
  PAYOUT_PROCESSING,

  // Platform revenue
  PLATFORM_REVENUE,
  SHIPPING_REVENUE,

  // Expenses
  PAYMENT_PROCESSING_EXPENSE,
  PAYOUT_EXPENSE,

  // Other liabilities
  TAX_PAYABLE,
  REFUND_PAYABLE,

  // Assets
  PLATFORM_CASH,
  PAYMENT_PROCESSOR_BALANCE
}