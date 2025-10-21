package com.nexo.document;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import lombok.*;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Document {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Set<String> RESERVED_FIELDS =
      Set.of("_id", "_score", "_version", "_source", "_collection", "_created_at");
  private static final int MAX_FIELD_NAME_LENGTH = 255;

  @JsonProperty("_id")
  @EqualsAndHashCode.Include
  private final String id;

  @JsonIgnore private final Map<String, Object> fields;

  @JsonProperty("_created_at")
  private final Instant createdAt;

  @JsonIgnore @Setter private boolean validated;

  private Document(String id, Map<String, Object> fields, Instant createdAt, boolean validated) {
    this.id = id;
    this.fields = new HashMap<>(fields != null ? fields : Map.of());
    this.createdAt = createdAt;
    this.validated = validated;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder()
        .id(this.id)
        .fields(new HashMap<>(this.fields))
        .createdAt(this.createdAt)
        .validated(this.validated);
  }

  public Object getFieldValue(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Field name cannot be null or blank");
    }
    return fields.get(name);
  }

  public boolean hasField(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Field name cannot be null or blank");
    }
    return fields.containsKey(name);
  }

  public Set<String> getFieldNames() {
    return Collections.unmodifiableSet(fields.keySet());
  }

  @JsonAnyGetter
  public Map<String, Object> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public String toJson(boolean prettyPrint) throws JsonProcessingException {
    return prettyPrint
        ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)
        : OBJECT_MAPPER.writeValueAsString(this);
  }

  public static Document fromJson(String json) throws JsonProcessingException {
    if (json == null || json.isBlank()) {
      throw new IllegalArgumentException("JSON string cannot be null or blank");
    }
    return OBJECT_MAPPER.readValue(json, Document.class);
  }

  @JsonAnySetter
  private void setDynamicField(String name, Object value) {
    fields.put(name, value);
  }

  public static class Builder {
    private String id;
    private Map<String, Object> fields;
    private Instant createdAt;
    private boolean validated;

    public Builder id(String id) {
      this.id = (id != null && !id.isBlank()) ? id.trim() : null;
      return this;
    }

    public Builder field(String name, Object value) {
      validateFieldName(name);
      validateFieldValue(value);
      if (this.fields == null) {
        this.fields = new HashMap<>();
      }
      this.fields.put(name, value);
      return this;
    }

    public Builder fields(Map<String, Object> fields) {
      if (fields != null) {
        fields.forEach(this::field);
      }
      return this;
    }

    public Builder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder validated(boolean validated) {
      this.validated = validated;
      return this;
    }

    public Document build() {
      String finalId = (id != null) ? id : UUID.randomUUID().toString();
      Map<String, Object> finalFields = (fields != null) ? Map.copyOf(fields) : Map.of();
      Instant finalCreatedAt = (createdAt != null) ? createdAt : Instant.now();
      return new Document(finalId, finalFields, finalCreatedAt, validated);
    }

    private void validateFieldName(String name) {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Field name cannot be null or blank");
      }
      if (RESERVED_FIELDS.contains(name)) {
        throw new IllegalArgumentException("Field name '" + name + "' is reserved");
      }
      if (name.startsWith("_")) {
        throw new IllegalArgumentException("Field names cannot start with underscore");
      }
      if (name.length() > MAX_FIELD_NAME_LENGTH) {
        throw new IllegalArgumentException("Field name too long: " + name.length());
      }
      if (!name.matches("[a-zA-Z0-9_.-]+")) {
        throw new IllegalArgumentException("Field name contains invalid characters: " + name);
      }
    }

    private void validateFieldValue(Object value) {
      if (value == null) {
        throw new IllegalArgumentException("Field value cannot be null");
      }
    }
  }
}
