package com.nexo.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexo.enums.RecordOption;
import com.nexo.enums.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class TextFieldOptions extends DefaultOptions {

  @JsonProperty("indexing")
  private Indexing indexing;

  @JsonProperty("vector")
  private Boolean vector;

  public TextFieldOptions() {
    super();
    setPrecision(null);
    setIndexed(null);
    setFieldNorm(null);
    this.vector = false;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Indexing {

    @JsonProperty("record")
    private RecordOption record;

    @JsonProperty("fieldnorms")
    private boolean fieldNorms;

    @JsonProperty("tokenizer")
    private Tokenizer tokenizer;
  }
}
