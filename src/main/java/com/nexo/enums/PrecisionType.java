package com.nexo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrecisionType {
  SECONDS("seconds"),
  MILLISECONDS("milliseconds"),
  MICROSECONDS("microseconds"),
  NANOSECONDS("nanoseconds");

  private final String value;

  PrecisionType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
