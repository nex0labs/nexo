package com.nexo.tantivy.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class Field {

  private String name;

  private FieldOptions options;

  private FieldType type;

  @JsonCreator
  public Field(
      @JsonProperty("name") String name,
      @JsonProperty("type") FieldType type,
      @JsonProperty("options") FieldOptions options) {
    this.name = name;
    this.type = type;
    this.options = copyFieldOptions(options);
  }

  public FieldOptions getOptions() {
    return copyFieldOptions(options);
  }

  private FieldOptions copyFieldOptions(FieldOptions original) {
    if (original == null) {
      return null;
    }
    return new FieldOptions(
        original.getIndex(),
        original.getStore(),
        original.getFast(),
        original.getTokenizer(),
        original.getIndexRecordOption());
  }
}
