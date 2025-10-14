package com.nexo.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexo.tantivy.schema.Field;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionMetadata {

  @JsonProperty("name")
  private String name;

  @JsonProperty("id")
  private String id;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;

  @JsonProperty("document_count")
  @Builder.Default
  private long documentCount = 0;

  @JsonProperty("fields")
  private List<Field> fields;

  @JsonProperty("status")
  @Builder.Default
  private CollectionStatus status = CollectionStatus.OPEN;

  @JsonProperty("size_in_bytes")
  @Builder.Default
  private long sizeInBytes = 0;
}
