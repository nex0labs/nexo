package com.nexo.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexo.enums.FieldType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class Field {

  private String name;

  private Object options;

  private FieldType type;

  @JsonCreator
  public Field(
      @JsonProperty("name") String name,
      @JsonProperty("type") FieldType type,
      @JsonProperty("options") Object options) {
    this.name = name;
    this.type = type;
    this.options = options;
  }
}
