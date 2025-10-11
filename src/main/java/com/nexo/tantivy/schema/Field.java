package com.nexo.tantivy.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Field {

  private final String name;

  private final FieldOptions options;

  private final FieldType type;

  public Field(String name, FieldType type, FieldOptions options) {
    this.name = name;
    this.type = type;
    this.options = copyFieldOptions(options);
  }

  public String getName() {
    return name;
  }

  public FieldType getType() {
    return type;
  }

  public FieldOptions getOptions() {
    return copyFieldOptions(options);
  }

  private FieldOptions copyFieldOptions(FieldOptions original) {
    if (original == null) {
      return null;
    }
    FieldOptions copy =
        new FieldOptions(
            original.getIndex(),
            original.getStore(),
            original.getFast(),
            original.getTokenizer(),
            original.getIndexRecordOption());
    return copy;
  }
}
