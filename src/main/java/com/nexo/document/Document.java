package com.nexo.document;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import lombok.*;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Document {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());
  private static final Set<String> RESERVED_FIELDS =
      Set.of("_id", "_score", "_version", "_timestamp", "_collection");
  private static final int MAX_FIELD_NAME_LENGTH = 255;
  private static final Pattern VALID_FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");

  @JsonProperty("_id")
  @EqualsAndHashCode.Include
  private final String id;

  @JsonIgnore private final Map<String, Object> fields;

  @JsonProperty("_timestamp")
  @EqualsAndHashCode.Include
  private final Instant createdAt;

  private Document(String id, Map<String, Object> fields, Instant createdAt) {
    this.id = id;
    this.fields = fields != null ? fields : Map.of();
    this.createdAt = createdAt;
  }

  public static DocumentBuilder builder() {
    return new DocumentBuilder();
  }

  public DocumentBuilder toBuilder() {
    return new DocumentBuilder()
        .id(this.id)
        .fields(new HashMap<>(this.fields))
        .createdAt(this.createdAt);
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

  @JsonIgnore
  public Set<String> getFieldNames() {
    return Collections.unmodifiableSet(fields.keySet());
  }

  @JsonAnyGetter
  public Map<String, Object> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public String toJsonString(boolean prettyPrint) throws JsonProcessingException {
    return prettyPrint
        ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)
        : OBJECT_MAPPER.writeValueAsString(this);
  }

  public static class DocumentBuilder {

    private String id;
    private Map<String, Object> fields;
    private Instant createdAt;

    public DocumentBuilder id(String id) {
      this.id = (id != null && !id.isBlank()) ? id.trim() : null;
      return this;
    }

    public DocumentBuilder field(String name, Object value) {
      validateFieldValue(value);
      if ("id".equals(name) && value instanceof String) {
        return id((String) value);
      }

      if (name != null && RESERVED_FIELDS.contains(name)) {
        if ("_id".equals(name) && value instanceof String) {
          return id((String) value);
        } else if ("_timestamp".equals(name) && value instanceof Instant) {
          return createdAt((Instant) value);
        }
        return this;
      }

      validateFieldName(name);
      if (this.fields == null) {
        this.fields = new HashMap<>();
      }
      this.fields.put(name, value);
      return this;
    }

    public DocumentBuilder fields(Map<String, Object> fields) {
      if (fields != null) {
        fields.forEach(this::field);
      }
      return this;
    }

    public DocumentBuilder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Document build() {
      String finalId = (id != null) ? id : UUID.randomUUID().toString();
      Map<String, Object> finalFields = (fields != null) ? Map.copyOf(fields) : Map.of();
      Instant finalCreatedAt = (createdAt != null) ? createdAt : Instant.now();
      return new Document(finalId, finalFields, finalCreatedAt);
    }

    private void validateFieldName(String name) {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Field name cannot be null or blank");
      }
      if (name.length() > MAX_FIELD_NAME_LENGTH) {
        throw new IllegalArgumentException("Field name too long: " + name.length());
      }
      if (name.startsWith("_")) {
        throw new IllegalArgumentException("Field names cannot start with underscore");
      }
      if (!VALID_FIELD_NAME_PATTERN.matcher(name).matches()) {
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
