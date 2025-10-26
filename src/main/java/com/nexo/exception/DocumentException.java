package com.nexo.exception;

public class DocumentException extends NexoException {

  public DocumentException(String message) {
    super(ErrorType.BAD_REQUEST, message);
  }

  public DocumentException(String message, Throwable cause) {
    super(ErrorType.BAD_REQUEST, message, cause);
  }
}
