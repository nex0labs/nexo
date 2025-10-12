package com.nexo.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

@Getter
public enum ErrorType {
  NOT_FOUND("NOT_FOUND", HttpResponseStatus.NOT_FOUND),
  BAD_REQUEST("BAD_REQUEST", HttpResponseStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpResponseStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED("UNAUTHORIZED", HttpResponseStatus.UNAUTHORIZED),
  FORBIDDEN("FORBIDDEN", HttpResponseStatus.FORBIDDEN),
  CONFLICT("CONFLICT", HttpResponseStatus.CONFLICT),
  UNPROCESSABLE_ENTITY("UNPROCESSABLE_ENTITY", HttpResponseStatus.UNPROCESSABLE_ENTITY),
  SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", HttpResponseStatus.SERVICE_UNAVAILABLE),
  GATEWAY_TIMEOUT("GATEWAY_TIMEOUT", HttpResponseStatus.GATEWAY_TIMEOUT);

  private final String code;
  private final HttpResponseStatus status;

  ErrorType(String code, HttpResponseStatus status) {
    this.code = code;
    this.status = status;
  }
}
