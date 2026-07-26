package com.benkih.estore.common.exceptions;

public class InsufficientReservedStockException extends RuntimeException {
  public InsufficientReservedStockException(String message) {
    super(message);
  }
}
