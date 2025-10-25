package com.nexo.document;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class DocumentTest {

  @Test
  public void testDocumentBuilderSuccess() throws JsonProcessingException {
    Document.DocumentBuilder docBuilder = Document.builder();
    docBuilder.field("title", "Sample Title");
    docBuilder.field("content", "Sample Content");
    Document document = docBuilder.build();
    System.out.println(document.toJsonString(true));
    assertEquals("Sample Title", document.getFieldValue("title"));
    assertEquals("Sample Content", document.getFieldValue("content"));
  }

  @Test
  public void testDocumentUnderscoreFields() {
    Document.DocumentBuilder docBuilder = Document.builder();
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> docBuilder.field("_title", "Sample Title"));
    assertEquals("Field names cannot start with underscore", exception.getMessage());
  }

  @Test
  public void testReservedFieldsHandledGracefully() {
    Document doc =
        Document.builder().field("_id", "my-custom-id").field("title", "Sample Title").build();

    assertEquals("my-custom-id", doc.getId());
    assertEquals("Sample Title", doc.getFieldValue("title"));
    assertFalse(doc.hasField("_id"));
  }

  @Test
  public void testReservedFieldsSilentlyIgnored() {
    Document doc =
        Document.builder()
            .field("_score", 1.5)
            .field("_version", 2)
            .field("title", "Sample Title")
            .build();

    assertEquals("Sample Title", doc.getFieldValue("title"));
    assertFalse(doc.hasField("_score"));
    assertFalse(doc.hasField("_version"));
  }

  @Test
  public void testNonCorrectField() {
    Document.DocumentBuilder docBuilder = Document.builder();
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> docBuilder.field("_title", "Sample Title"));
    assertEquals("Field names cannot start with underscore", exception.getMessage());
  }
}
