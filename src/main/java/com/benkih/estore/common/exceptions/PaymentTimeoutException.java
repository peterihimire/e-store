package com.benkih.estore.common.exceptions;

public class PaymentTimeoutException extends RuntimeException {
  public PaymentTimeoutException(String message) {
    super(message);
  }
}
