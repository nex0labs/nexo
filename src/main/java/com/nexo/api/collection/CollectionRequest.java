package com.nexo.api.collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionRequest {

  @NotBlank(message = "Collection name is required")
  @Pattern(
      regexp = "^[a-z][a-z0-9_-]*$",
      message =
          "Collection name must start with a lowercase letter and contain only lowercase alphanumeric characters, hyphens, and underscores")
  private String name;

  @NotEmpty(message = "At least one field is required")
  @Valid
  private List<FieldRequest> fields;
}
