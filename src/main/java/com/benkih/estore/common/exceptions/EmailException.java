package com.benkih.estore.common.exceptions;

public class EmailException extends RuntimeException  {
  public EmailException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailException(String message) {
    super(message);
  }
}
