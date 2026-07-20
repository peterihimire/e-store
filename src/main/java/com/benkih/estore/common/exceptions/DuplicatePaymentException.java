package com.benkih.estore.common.exceptions;

public class DuplicatePaymentException  extends RuntimeException {
  public DuplicatePaymentException(String message) {
    super(message);
  }
}
