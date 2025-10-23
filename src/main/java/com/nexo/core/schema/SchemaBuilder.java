package com.nexo.core.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaBuilder {

  private final List<Field> fields = new ArrayList<>();

  public List<Field> getFields() {
    return Collections.unmodifiableList(fields);
  }

  public void addField(String name, FieldType type, FieldOptions options) {
    fields.add(new Field(name, type, options));
  }

  public void addField(String name, FieldType type, FieldFlag... flags) {
    FieldOptions options = FieldOptions.defaultsFor(type);

    if (flags != null) {
      for (FieldFlag flag : flags) {
        options =
            switch (flag) {
              case INDEXED -> options.withIndexed(true);
              case STORED -> options.withStored(true);
              case FACET -> options.withFacet(true);
            };
      }
    }

    addField(name, type, options);
  }

  public String toJson() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fields);
  }

  public static SchemaBuilder fromJson(String json) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<Field> fields = Arrays.asList(mapper.readValue(json, Field[].class));
    SchemaBuilder builder = new SchemaBuilder();
    builder.fields.addAll(fields);
    return builder;
  }
}
