package com.benkih.estore.common.exceptions;


public class DuplicatePayoutException  extends RuntimeException {
  public DuplicatePayoutException(String message) {
    super(message);
  }
}