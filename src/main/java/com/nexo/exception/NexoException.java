package com.nexo.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

@Getter
public class NexoException extends RuntimeException {
  private final ErrorType errorType;
  private final HttpResponseStatus status;
  private final String errorCode;

  public NexoException(ErrorType errorType, String message) {
    super(message);
    this.errorType = errorType;
    this.status = errorType.getStatus();
    this.errorCode = errorType.getCode();
  }

  public NexoException(ErrorType errorType, String message, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
    this.status = errorType.getStatus();
    this.errorCode = errorType.getCode();
  }

  public static NexoException notFound(String message) {
    return new NexoException(ErrorType.NOT_FOUND, message);
  }

  public static NexoException badRequest(String message) {
    return new NexoException(ErrorType.BAD_REQUEST, message);
  }

  public static NexoException internalError(String message) {
    return new NexoException(ErrorType.INTERNAL_SERVER_ERROR, message);
  }

  public static NexoException internalError(String message, Throwable cause) {
    return new NexoException(ErrorType.INTERNAL_SERVER_ERROR, message, cause);
  }

  public static NexoException unauthorized(String message) {
    return new NexoException(ErrorType.UNAUTHORIZED, message);
  }

  public static NexoException forbidden(String message) {
    return new NexoException(ErrorType.FORBIDDEN, message);
  }

  public static NexoException conflict(String message) {
    return new NexoException(ErrorType.CONFLICT, message);
  }

  public static NexoException unprocessableEntity(String message) {
    return new NexoException(ErrorType.UNPROCESSABLE_ENTITY, message);
  }
}
