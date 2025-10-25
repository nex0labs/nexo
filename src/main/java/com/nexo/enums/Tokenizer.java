package com.nexo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Tokenizer {
  DEFAULT("default"),
  RAW("raw");

  private final String value;

  Tokenizer(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
