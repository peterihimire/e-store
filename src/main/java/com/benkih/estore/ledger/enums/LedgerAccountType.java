package com.benkih.estore.ledger.enums;

public enum LedgerAccountType {

  // Seller money
  SELLER_PENDING,
  SELLER_AVAILABLE,
  SELLER_RESERVED,

  // Platform
  PLATFORM_REVENUE,
  PAYMENT_PROCESSING_FEES,
  SHIPPING_REVENUE,

  // Liabilities
  TAX_PAYABLE,
  REFUND_PAYABLE,

  // Payout lifecycle
  PAYOUT_PROCESSING
}
