package com.nexo.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class NotFoundException extends NexoException {
  public NotFoundException(String message) {
    super(HttpResponseStatus.NOT_FOUND, message);
  }

  public NotFoundException(String message, String errorCode) {
    super(HttpResponseStatus.NOT_FOUND, message, errorCode);
  }
}
