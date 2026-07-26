package com.benkih.estore.common.exceptions;

public class InvalidInventoryQuantityException extends RuntimeException {
  public InvalidInventoryQuantityException(String message) {
    super(message);
  }
}
