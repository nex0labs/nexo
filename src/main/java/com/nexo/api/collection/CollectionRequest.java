package com.nexo.api.collection;

import com.nexo.tantivy.schema.Field;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionRequest {
  private String name;
  private List<Field> fields;
}
