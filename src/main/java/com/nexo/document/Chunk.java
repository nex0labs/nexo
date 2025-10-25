package com.nexo.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Chunk {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @JsonProperty("_chunk_id")
  @EqualsAndHashCode.Include
  private final String chunkId;

  @JsonProperty("_chunk_index")
  private final int chunkIndex;

  @JsonProperty("_chunk_start_position")
  private final int startPosition;

  @JsonProperty("_chunk_end_position")
  private final int endPosition;

  @JsonProperty("chunk_text")
  private final String text;

  @JsonProperty("chunk_embeddings")
  private final float[] embeddings;

  @JsonIgnore
  public boolean hasEmbedding() {
    return embeddings != null && embeddings.length > 0;
  }

  @JsonIgnore
  public int getEmbeddingDimension() {
    return embeddings != null ? embeddings.length : 0;
  }

  @JsonIgnore
  public int getChunkSize() {
    return text != null ? text.length() : 0;
  }

  public String toJson(boolean prettyPrint) throws JsonProcessingException {
    return prettyPrint
        ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)
        : OBJECT_MAPPER.writeValueAsString(this);
  }

  public static Chunk fromJson(String json) throws JsonProcessingException {
    if (json == null || json.isBlank()) {
      throw new IllegalArgumentException("JSON string cannot be null or blank");
    }
    return OBJECT_MAPPER.readValue(json, Chunk.class);
  }
}
