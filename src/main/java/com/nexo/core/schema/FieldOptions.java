package com.nexo.core.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public record FieldOptions(
    @JsonProperty("index") Boolean indexed,
    @JsonProperty("store") Boolean stored,
    @JsonProperty("fast") Boolean fast,
    @JsonProperty("tokenizer") String tokenizer,
    @JsonProperty("index_record_option") String indexRecordOption) {

  public boolean isIndexed() {
    return Boolean.TRUE.equals(indexed);
  }

  public boolean isStored() {
    return Boolean.TRUE.equals(stored);
  }

  public boolean isFast() {
    return Boolean.TRUE.equals(fast);
  }

  public static FieldOptions text(String tokenizer) {
    return new FieldOptions(
        true,
        true,
        false,
        tokenizer == null || tokenizer.isBlank() ? "default" : tokenizer,
        "WithFreqsAndPositions");
  }

  public static FieldOptions numeric() {
    return new FieldOptions(true, true, false, null, null);
  }

  public static FieldOptions date() {
    return new FieldOptions(true, true, false, null, null);
  }

  public static FieldOptions bool() {
    return new FieldOptions(true, true, false, null, null);
  }

  public FieldOptions withIndexed(boolean indexed) {
    return new FieldOptions(indexed, stored, fast, tokenizer, indexRecordOption);
  }

  public FieldOptions withStored(boolean stored) {
    return new FieldOptions(indexed, stored, fast, tokenizer, indexRecordOption);
  }

  public FieldOptions withFacet(boolean facet) {
    return new FieldOptions(indexed, stored, facet, tokenizer, indexRecordOption);
  }

  public FieldOptions withTokenizer(String tokenizer) {
    return new FieldOptions(indexed, stored, fast, tokenizer, indexRecordOption);
  }

  public FieldOptions withIndexRecordOption(String recordOption) {
    return new FieldOptions(indexed, stored, fast, tokenizer, recordOption);
  }

  public static FieldOptions defaultsFor(FieldType type) {
    return switch (type) {
      case TEXT -> text("default");
      case U64, I64, F64, BYTES, DATE -> numeric();
      case VECTOR ->
          throw new IllegalArgumentException(
              "VECTOR type should not be used. Use TEXT type with vector flag instead");
      case BOOL -> bool();
    };
  }
}
