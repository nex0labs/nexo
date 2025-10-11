package com.nexo.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class InternalServerException extends NexoException {
  public InternalServerException(String message) {
    super(HttpResponseStatus.INTERNAL_SERVER_ERROR, message);
  }

  public InternalServerException(String message, Throwable cause) {
    super(HttpResponseStatus.INTERNAL_SERVER_ERROR, message, cause);
  }

  public InternalServerException(String message, String errorCode, Throwable cause) {
    super(HttpResponseStatus.INTERNAL_SERVER_ERROR, message, errorCode, cause);
  }
}
