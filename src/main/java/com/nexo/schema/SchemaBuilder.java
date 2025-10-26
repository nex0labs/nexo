package com.nexo.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexo.enums.FieldType;
import com.nexo.enums.PrecisionType;
import com.nexo.enums.RecordOption;
import com.nexo.enums.Tokenizer;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaBuilder {

  private final List<Field> schemaFields = new ArrayList<>();

  public SchemaBuilder() {
    addSystemField();
  }

  public List<Field> getFields() {
    return Collections.unmodifiableList(schemaFields);
  }

  private void addSystemField() {
    addTextField("_id", true, true, Tokenizer.RAW, RecordOption.BASIC);
    addDateField("_timestamp");
  }

  public void addTextField(
      String name, boolean facet, boolean stored, Tokenizer tokenizer, RecordOption record) {
    if (tokenizer == null) {
      tokenizer = Tokenizer.DEFAULT;
    }
    if (record == null) {
      record = RecordOption.BASIC;
    }

    new TextFieldBuilder(name) {
      @Override
      public Field build() {
        Field field = super.build();
        schemaFields.add(field);
        return field;
      }
    }.facet(facet).stored(stored).tokenizer(tokenizer).record(record).build();
  }

  public void addTextField(String name) {
    new TextFieldBuilder(name) {
      @Override
      public Field build() {
        Field field = super.build();
        schemaFields.add(field);
        return field;
      }
    }.build();
  }

  public void addNumericField(String name, FieldType type) {
    switch (type) {
      case I64, F64, U64 -> {
        new NumericFieldBuilder(name, type) {
          @Override
          public Field build() {
            Field field = super.build();
            schemaFields.add(field);
            return field;
          }
        }.build();
      }
      default ->
          throw new IllegalArgumentException(
              "Invalid numeric type: " + type + ". Use I64, F64, or U64");
    }
  }

  public void addNumericField(
      String name,
      FieldType type,
      boolean indexed,
      boolean stored,
      boolean facet,
      boolean fieldNorms) {
    switch (type) {
      case I64, F64, U64 -> {
        new NumericFieldBuilder(name, type) {
          @Override
          public Field build() {
            Field field = super.build();
            schemaFields.add(field);
            return field;
          }
        }.indexed(indexed).stored(stored).fast(facet).facet(facet).fieldNorms(fieldNorms).build();
      }
      default ->
          throw new IllegalArgumentException(
              "Invalid numeric type: " + type + ". Use I64, F64, or U64");
    }
  }

  public void addDateField(
      String name,
      boolean indexed,
      boolean stored,
      boolean facet,
      boolean fieldNorms,
      PrecisionType precision) {
    new NumericFieldBuilder(name, FieldType.DATE) {
      @Override
      public Field build() {
        Field field = super.build();
        schemaFields.add(field);
        return field;
      }
    }.indexed(indexed)
        .stored(stored)
        .fast(facet)
        .facet(facet)
        .fieldNorms(fieldNorms)
        .precision(precision)
        .build();
  }

  public void addDateField(String name) {
    new NumericFieldBuilder(name, FieldType.DATE) {
      @Override
      public Field build() {
        Field field = super.build();
        schemaFields.add(field);
        return field;
      }
    }.precision(PrecisionType.SECONDS).build();
  }

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(schemaFields);
  }

  public static SchemaBuilder fromJson(String json) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<Field> fields = Arrays.asList(mapper.readValue(json, Field[].class));
    SchemaBuilder builder = new SchemaBuilder();
    builder.schemaFields.addAll(fields);
    return builder;
  }
}
