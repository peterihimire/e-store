package com.benkih.estore.common.exceptions;

public class PaymentVerificationException extends RuntimeException {
  public PaymentVerificationException(String message) {
    super(message);
  }
}
