package com.nexo.tantivy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import lombok.Getter;

public class Document {
  @Getter private final String id;
  private final Map<String, Object> fields = new HashMap<>();

  public Document(String id) {
    if (id == null || id.trim().isEmpty()) {
      this.id = UUID.randomUUID().toString();
    } else {
      this.id = id;
    }
    fields.put("id", this.id);
  }

  public void addField(String name, Object value) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Field name cannot be null and cannot be empty");
    }
    if (value == null) {
      throw new IllegalArgumentException("Field value cannot be null");
    }
    fields.put(name, value);
  }

  public void removeFields(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Field name cannot be null");
    }
    fields.remove(name);
  }

  public Map<String, Object> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public Object getFieldValue(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Field name cannot be null");
    }
    return fields.get(name);
  }

  public String toJson() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fields);
  }
}
