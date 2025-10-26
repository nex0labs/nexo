package com.nexo.schema;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexo.enums.FieldType;
import com.nexo.enums.PrecisionType;
import com.nexo.enums.RecordOption;
import com.nexo.enums.Tokenizer;
import org.junit.jupiter.api.Test;

class SchemaBuilderTest {

  @Test
  void addTestFieldTest() throws JsonProcessingException {
    SchemaBuilder schemaBuilder = new SchemaBuilder();
    schemaBuilder.addTextField("t1");
    schemaBuilder.addTextField("t2", false, false, Tokenizer.RAW, RecordOption.POSITION);
    schemaBuilder.addTextField("t3", false, false, Tokenizer.DEFAULT, RecordOption.BASIC);
    String jsonString =
        """
            [{"name":"_id","type":"text","options":{"stored":true,"fast":true,"indexing":{"record":"basic","fieldnorms":true,"tokenizer":"raw"}}},{"name":"_timestamp","type":"date","options":{"indexed":false,"stored":true,"precision":"seconds","fieldnorms":false,"fast":true}},{"name":"t1","type":"text","options":{"stored":true,"fast":false,"indexing":{"record":"basic","fieldnorms":true,"tokenizer":"default"}}},{"name":"t2","type":"text","options":{"stored":false,"fast":false,"indexing":{"record":"WithFreqsAndPositions","fieldnorms":true,"tokenizer":"raw"}}},{"name":"t3","type":"text","options":{"stored":false,"fast":false,"indexing":{"record":"basic","fieldnorms":true,"tokenizer":"default"}}}]
            """;
    assertEquals(jsonString.trim(), schemaBuilder.toJsonString());
  }

  @Test
  void addNumericFieldTest() throws JsonProcessingException {
    SchemaBuilder schemaBuilder = new SchemaBuilder();
    schemaBuilder.addNumericField("n1", FieldType.F64);
    schemaBuilder.addNumericField("n2", FieldType.I64, true, true, true, true);
    String jsonString =
        """
               [{"name":"_id","type":"text","options":{"stored":true,"fast":true,"indexing":{"record":"basic","fieldnorms":true,"tokenizer":"raw"}}},{"name":"_timestamp","type":"date","options":{"indexed":false,"stored":true,"precision":"seconds","fieldnorms":false,"fast":true}},{"name":"n1","type":"f64","options":{"indexed":false,"stored":true,"fieldnorms":false,"fast":true}},{"name":"n2","type":"i64","options":{"indexed":true,"stored":true,"fieldnorms":true,"fast":true}}]
                """;
    assertEquals(jsonString.trim(), schemaBuilder.toJsonString());
  }

  @Test
  void addDateFieldTest() throws JsonProcessingException {
    SchemaBuilder schemaBuilder = new SchemaBuilder();
    schemaBuilder.addDateField("d1");
    schemaBuilder.addDateField("d2", true, true, true, true, PrecisionType.MILLISECONDS);
    String jsonString =
        """
                [{"name":"_id","type":"text","options":{"stored":true,"fast":true,"indexing":{"record":"basic","fieldnorms":true,"tokenizer":"raw"}}},{"name":"_timestamp","type":"date","options":{"indexed":false,"stored":true,"precision":"seconds","fieldnorms":false,"fast":true}},{"name":"d1","type":"date","options":{"indexed":false,"stored":true,"precision":"seconds","fieldnorms":false,"fast":true}},{"name":"d2","type":"date","options":{"indexed":true,"stored":true,"precision":"milliseconds","fieldnorms":true,"fast":true}}]
                """;
    assertEquals(jsonString.trim(), schemaBuilder.toJsonString());
  }
}
