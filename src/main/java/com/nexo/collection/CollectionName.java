package com.nexo.collection;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class CollectionName {

  @NonNull String value;

  public CollectionName(@NonNull String value) {
    if (value.isBlank()) {
      throw new IllegalArgumentException("Collection name cannot be blank");
    }
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
