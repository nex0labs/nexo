package com.nexo.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class NexoException extends RuntimeException {
  private final HttpResponseStatus status;
  private final String errorCode;

  public NexoException(HttpResponseStatus status, String message) {
    super(message);
    this.status = status;
    this.errorCode = status.code() + "";
  }

  public NexoException(HttpResponseStatus status, String message, String errorCode) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }

  public NexoException(HttpResponseStatus status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.errorCode = status.code() + "";
  }

  public NexoException(
      HttpResponseStatus status, String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.errorCode = errorCode;
  }

  public HttpResponseStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
