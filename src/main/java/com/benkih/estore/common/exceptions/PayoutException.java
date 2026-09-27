package com.benkih.estore.common.exceptions;


public class PayoutException  extends RuntimeException {
  public PayoutException (String message) {
    super(message);
  }
  public PayoutException (String message, Throwable cause) {
    super(message, cause);
  }
}
