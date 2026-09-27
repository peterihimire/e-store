package com.benkih.estore.common.exceptions;


public class PayoutGatewayException extends RuntimeException {
  public PayoutGatewayException(String message) {
    super(message);
  }
  public PayoutGatewayException(String message, Throwable cause) {
    super(message, cause);
  }
}