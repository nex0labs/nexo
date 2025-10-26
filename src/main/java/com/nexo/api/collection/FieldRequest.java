package com.nexo.api.collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexo.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldRequest {

  @NotBlank(message = "Field name is required")
  @Pattern(
      regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$",
      message =
          "Field name must start with a letter and contain only alphanumeric characters, underscores, and hyphens")
  private String name;

  @NotNull(message = "Field type is required")
  private FieldType type;

  @JsonProperty("facet")
  private boolean facet;

  @JsonProperty("vector")
  private boolean vector;

  public void validate() {
    if (vector && type != FieldType.TEXT) {
      throw new IllegalArgumentException(
          String.format(
              "Field '%s' marked as vector but has type '%s'. Vector fields must have type 'TEXT'.",
              name, type));
    }

    if (type == FieldType.VECTOR) {
      throw new IllegalArgumentException(
          String.format(
              "Field '%s' has type 'VECTOR' which is not allowed. Use type 'TEXT' with vector=true instead.",
              name));
    }
  }
}
