package com.nexo.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class BadRequestException extends NexoException {
  public BadRequestException(String message) {
    super(HttpResponseStatus.BAD_REQUEST, message);
  }

  public BadRequestException(String message, String errorCode) {
    super(HttpResponseStatus.BAD_REQUEST, message, errorCode);
  }

  public BadRequestException(String message, Throwable cause) {
    super(HttpResponseStatus.BAD_REQUEST, message, cause);
  }
}
