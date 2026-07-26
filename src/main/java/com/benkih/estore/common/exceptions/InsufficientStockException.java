package com.benkih.estore.common.exceptions;

public class InsufficientStockException extends RuntimeException {
  public InsufficientStockException(String message) {
    super(message);
  }
}
