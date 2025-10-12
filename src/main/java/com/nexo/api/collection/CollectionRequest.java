package com.nexo.api.collection;

import com.nexo.tantivy.schema.Field;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CollectionRequest {
  private String name;
  private List<Field> fields;
}
