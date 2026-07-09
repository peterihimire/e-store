package com.benkih.estore.common.enums;

public enum PaymentEventType {
  INITIALIZED,
  PENDING,
  AUTHORIZED,
  SUCCESS,
  FAILED,
  ABANDONED,
  CANCELLED,
  REFUNDED,
  WEBHOOK_RECEIVED,
  WEBHOOK_VERIFIED
}