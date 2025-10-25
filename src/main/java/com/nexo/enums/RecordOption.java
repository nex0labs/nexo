package com.nexo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecordOption {
  BASIC("basic"),
  POSITION("WithFreqsAndPositions"),
  FREQ("WithFreqs");

  private final String value;

  RecordOption(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
