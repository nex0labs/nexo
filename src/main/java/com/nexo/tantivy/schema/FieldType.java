package com.nexo.tantivy.schema;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {
  TEXT("text"),
  I64("i64"),
  F64("f64"),
  BYTES("bytes"),
  U64("u64"),
  DATE("date");
  private final String name;

  FieldType(String name) {
    this.name = name;
  }

  @JsonValue
  public String getJsonValue() {
    return name;
  }
}
