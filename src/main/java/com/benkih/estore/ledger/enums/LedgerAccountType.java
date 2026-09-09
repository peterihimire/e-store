package com.benkih.estore.ledger.enums;


public enum LedgerAccountType {

  // =========================
  // ASSETS
  // =========================

  /**
   * Benkih's actual cash held in bank/payment accounts.
   */
  PLATFORM_CASH,

  /**
   * Money currently held by a payment processor
   * but belonging to Benkih/sellers.
   */
  PAYMENT_PROCESSOR_BALANCE,


  // =========================
  // LIABILITIES
  // =========================

  CUSTOMER_PAYMENT_LIABILITY,
  /**
   * Money owed to sellers but not yet eligible for release.
   */
  SELLER_PENDING,

  /**
   * Money owed to sellers and available for payout.
   */
  SELLER_AVAILABLE,

  /**
   * Seller funds temporarily reserved,
   * e.g. while a payout is being processed.
   */
  SELLER_RESERVED,

  /**
   * Money currently being transferred to a seller.
   */
  PAYOUT_PROCESSING,

  /**
   * Taxes collected/owed to government or tax authorities.
   */
  TAX_PAYABLE,

  /**
   * Amounts owed to customers because of refunds.
   */
  REFUND_PAYABLE,


  // =========================
  // REVENUE
  // =========================

  /**
   * Benkih's marketplace/platform commission.
   */
  PLATFORM_REVENUE,

  /**
   * Shipping amount collected from customers
   * when Benkih is entitled to the shipping revenue.
   */
  SHIPPING_REVENUE,


  // =========================
  // EXPENSES
  // =========================

  /**
   * Fees charged by Paystack/other payment processors.
   */
  PAYMENT_PROCESSING_EXPENSE,

  /**
   * Fees incurred by Benkih when sending payouts.
   */
  PAYOUT_EXPENSE
}
//public enum LedgerAccountType {
//
//  // Seller money
//  SELLER_PENDING,
//  SELLER_AVAILABLE,
//  SELLER_RESERVED,
//
//  // Platform
//  PLATFORM_REVENUE,
//  PLATFORM_CASH,
//  PLATFORM_EXPENSE,
//
//  PAYMENT_PROCESSING_FEES,
//  SHIPPING_REVENUE,
//
//  // Liabilities
//  TAX_PAYABLE,
//  REFUND_PAYABLE,
//
//  // Payout lifecycle
//  PAYOUT_PROCESSING
//}
