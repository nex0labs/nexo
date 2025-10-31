package com.nexo.document;

import static org.junit.jupiter.api.Assertions.*;

import com.nexo.collection.Collection;
import com.nexo.collection.CollectionManager;
import com.nexo.collection.CollectionName;
import com.nexo.schema.SchemaBuilder;
import com.nexo.testutil.TestSchemaUtils;
import org.junit.jupiter.api.Test;

class DocumentManagerTest {

  // @Test
  //  void documentAddTest() throws JsonProcessingException {
  //    Collection collection = setUpCollection(CollectionName.of("test-collection"));
  //    DocumentManager documentManager = new DocumentManager(collection);
  //
  //    DocumentBuilder doc = Document.builder();
  //    doc.field("title", "Test Document");
  //    doc.field("content", "This is a test document.");
  //    doc.field("url", "http://example.com/test-document");
  //    assertDoesNotThrow(() -> documentManager.addDocument(doc.build()));
  //  }

  private Collection setUpCollection(CollectionName collectionName) {
    CollectionManager collectionManager = CollectionManager.getInstance();
    if (!collectionManager.collectionExists(collectionName)) {
      SchemaBuilder schemaBuilder = TestSchemaUtils.getTestSchema();
      collectionManager.createCollection(collectionName, schemaBuilder);
    }

    return collectionManager.getCollection(collectionName);
  }

  @Test
  void addDocumentsFromJSON() throws Exception {
    Collection collection = setUpCollection(CollectionName.of("test-json-collection"));
    DocumentManager documentManager = new DocumentManager(collection);
    String jsonArray = readJsonlFromResource();

    assertDoesNotThrow(() -> documentManager.addDocuments(jsonArray));
  }

  private String readJsonlFromResource() throws Exception {
    try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("sample.jsonl")) {
      assertNotNull(is, "sample.jsonl" + " should exist in test resources");

      try (java.io.BufferedReader reader =
          new java.io.BufferedReader(
              new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {

        String lines =
            reader
                .lines()
                .filter(line -> !line.trim().isEmpty())
                .collect(java.util.stream.Collectors.joining(","));

        return "[" + lines + "]";
      }
    }
  }
}
