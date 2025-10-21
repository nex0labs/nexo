package com.nexo.exception;

public class CollectionException extends RuntimeException {
  public CollectionException(String message) {
    super(message);
  }

  public CollectionException(String message, Throwable cause) {
    super(message, cause);
  }

  public CollectionException(Throwable cause) {
    super(cause);
  }
}
