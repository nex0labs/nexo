package com.nexo.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexo.collection.Collection;
import com.nexo.document.Document.DocumentBuilder;
import com.nexo.enums.FieldType;
import com.nexo.exception.DocumentException;
import com.nexo.schema.Field;
import com.nexo.schema.TextFieldOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentManager {

  private final Collection collection;
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());
  private static final String ID_FIELD = "id";
  private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF =
      new TypeReference<>() {};

  private final Map<String, Field> schemaFieldsCache;

  public DocumentManager(Collection collection) {
    this.collection = collection;
    List<Field> fieldsList = collection.getMetadata().getFields();
    this.schemaFieldsCache = fieldsList.stream().collect(Collectors.toMap(Field::getName, f -> f));
  }

  private void addDocument(Document doc) throws JsonProcessingException {
    this.collection.getTantivyIndex().addDocument(doc);
  }

  private void addDocuments(List<Document> docs) throws JsonProcessingException {
    this.collection.getTantivyIndex().addDocuments(docs);
  }

  public void addDocument(String jsonDocument) {
    if (jsonDocument == null || jsonDocument.isEmpty()) {
      throw new DocumentException("Documents cannot be null or empty");
    }
    try {
      HashMap<String, Object> documentsFields =
          OBJECT_MAPPER.readValue(jsonDocument, new TypeReference<>() {});
      Document doc = docFromFields(documentsFields);
      addDocument(doc);
    } catch (JsonProcessingException e) {
      throw new DocumentException("Failed to parse JSON documents", e);
    }
  }

  private Document docFromFields(Map<String, Object> fields) {
    DocumentBuilder docBuilder = Document.builder();
    if (fields.containsKey(ID_FIELD)) {
      Object idValue = fields.get(ID_FIELD);
      if (idValue != null) {
        docBuilder.id(idValue.toString());
      }
      fields.remove(ID_FIELD);
    }
    for (String key : fields.keySet()) {
      if (!schemaFieldsCache.containsKey(key)) {
        log.warn("Field '{}' is not defined in the collection schema", key);
      } else if (schemaFieldsCache.get(key).getType() == FieldType.TEXT) {

        docBuilder.field(key, fields.get(key));
      } else {
        docBuilder.field(key, fields.get(key));
      }
    }
    return docBuilder.build();
  }

  private boolean isVectorField(Field field) {
    if (field.getType() == FieldType.TEXT) {
      Object options = field.getOptions();
      TextFieldOptions textOptions =
          options instanceof TextFieldOptions
              ? (TextFieldOptions) options
              : OBJECT_MAPPER.convertValue(options, TextFieldOptions.class);
      return textOptions.getVector() != null && textOptions.getVector();
    }
    return false;
  }

  public void addDocuments(String jsonArray) {
    if (jsonArray == null || jsonArray.isEmpty()) {
      throw new DocumentException("Documents cannot be null or empty");
    }
    try {
      List<HashMap<String, Object>> documentsFields =
          OBJECT_MAPPER.readValue(jsonArray, new TypeReference<>() {});
      List<Document> documents = new java.util.ArrayList<>();
      for (HashMap<String, Object> fields : documentsFields) {
        Document doc = docFromFields(fields);
        documents.add(doc);
      }
      addDocuments(documents);

    } catch (JsonProcessingException e) {
      throw new DocumentException("Failed to parse JSON documents", e);
    }
  }
}
