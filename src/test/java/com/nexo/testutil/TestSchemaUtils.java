package com.nexo.testutil;

import com.nexo.enums.FieldType;
import com.nexo.schema.SchemaBuilder;

public class TestSchemaUtils {

  public static SchemaBuilder getTestSchema() {
    SchemaBuilder schemaBuilder = new SchemaBuilder();
    schemaBuilder.addTextField("title");
    schemaBuilder.addTextField("content", true);
    schemaBuilder.addNumericField("views", FieldType.I64);
    return schemaBuilder;
  }
}
