package com.nexo.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexo.collection.Collection;
import com.nexo.document.Document.DocumentBuilder;
import com.nexo.exception.DocumentException;
import com.nexo.schema.Field;
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
      new TypeReference<HashMap<String, Object>>() {};

  private final Map<String, Field> schemaFieldsCache;

  public DocumentManager(Collection collection) {
    this.collection = collection;
    List<Field> fieldsList = collection.getMetadata().getFields();
    this.schemaFieldsCache = fieldsList.stream().collect(Collectors.toMap(Field::getName, f -> f));
  }

  public void addDocument(Document doc) throws JsonProcessingException {
    this.collection.getTantivyIndex().addDocument(doc);
  }

  public void addDocuments(List<Document> docs) throws JsonProcessingException {
    this.collection.getTantivyIndex().addDocuments(docs);
  }

  public void addDocument(String jsonDocuments) {
    if (jsonDocuments == null || jsonDocuments.isEmpty()) {
      throw new DocumentException("Documents cannot be null or empty");
    }
    try {
      List<HashMap<String, Object>> documentsFields =
          OBJECT_MAPPER.readValue(
              jsonDocuments, new TypeReference<List<HashMap<String, Object>>>() {});
      List<Document> documents = new java.util.ArrayList<>();
      for (HashMap<String, Object> fields : documentsFields) {
        DocumentBuilder document = Document.builder();

        for (String key : fields.keySet()) {
          if (key.equals(ID_FIELD)) {
            document.id((String) fields.get(key));
          } else if (!schemaFieldsCache.containsKey(key)) {
            log.warn("Field '{}' is not defined in the collection schema", key);
          } else {
            document.field(key, fields.get(key));
          }
        }
        Document doc = document.build();
        log.debug("Adding document: {}", doc);
        documents.add(doc);
      }

      addDocuments(documents);

    } catch (JsonProcessingException e) {
      throw new DocumentException("Failed to parse JSON documents", e);
    }
  }
}
