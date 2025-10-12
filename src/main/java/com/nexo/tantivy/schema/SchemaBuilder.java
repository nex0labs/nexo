package com.nexo.tantivy.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaBuilder {

  private final List<Field> fields = new ArrayList<>();

  public List<Field> getFields() {
    return Collections.unmodifiableList(fields);
  }

  public SchemaBuilder addTextField(String name, String tokenizer, FieldFlag... flags) {
    return addField(name, FieldType.TEXT, FieldOptions.text(tokenizer), flags);
  }

  public SchemaBuilder addU64Field(String name, FieldFlag... flags) {
    return addField(name, FieldType.U64, FieldOptions.u64(), flags);
  }

  public SchemaBuilder addI64Field(String name, FieldFlag... flags) {
    return addField(name, FieldType.I64, FieldOptions.i64(), flags);
  }

  public SchemaBuilder addF64Field(String name, FieldFlag... flags) {
    return addField(name, FieldType.F64, FieldOptions.f64(), flags);
  }

  public SchemaBuilder addBytesField(String name, FieldFlag... flags) {
    return addField(name, FieldType.BYTES, FieldOptions.bytes(), flags);
  }

  public SchemaBuilder addDateField(String name, FieldFlag... flags) {
    return addField(name, FieldType.DATE, FieldOptions.date(), flags);
  }

  private SchemaBuilder addField(
      String name, FieldType type, FieldOptions baseOptions, FieldFlag... flags) {
    EnumSet<FieldFlag> flagSet = EnumSet.noneOf(FieldFlag.class);
    Collections.addAll(flagSet, flags);

    FieldOptions options =
        baseOptions
            .indexed(flagSet.contains(FieldFlag.INDEXED))
            .stored(flagSet.contains(FieldFlag.STORED))
            .fast(flagSet.contains(FieldFlag.FAST));

    fields.add(new Field(name, type, options));
    return this;
  }

  public String toJson() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonString = objectMapper.writeValueAsString(this);
    JsonNode root = objectMapper.readTree(jsonString);
    JsonNode fieldsArray = root.get("fields");
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fieldsArray);
  }

  public static SchemaBuilder fromJson(String json) throws JsonProcessingException {
    return new ObjectMapper().readValue(json, SchemaBuilder.class);
  }
}
