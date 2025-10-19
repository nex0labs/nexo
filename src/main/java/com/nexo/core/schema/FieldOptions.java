package com.nexo.core.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class FieldOptions {

    private final Boolean index;
    private final Boolean store;
    private final Boolean fast;
    private final String tokenizer;

    @JsonProperty("index_record_option")
    private final String indexRecordOption;

    @JsonCreator
    public FieldOptions(
        @JsonProperty("index") Boolean index,
        @JsonProperty("store") Boolean store,
        @JsonProperty("fast") Boolean fast,
        @JsonProperty("tokenizer") String tokenizer,
        @JsonProperty("index_record_option") String indexRecordOption) {
        this.index = index;
        this.store = store;
        this.fast = fast;
        this.tokenizer = tokenizer;
        this.indexRecordOption = indexRecordOption;
    }

    public static FieldOptions text(String tokenizer) {
        return new FieldOptions(null, null, null, tokenizer, "WithFreqsAndPositions");
    }

    public static FieldOptions u64() {
        return new FieldOptions(null, null, null, null, null);
    }

    public static FieldOptions i64() {
        return new FieldOptions(null, null, null, null, null);
    }

    public static FieldOptions f64() {
        return new FieldOptions(null, null, null, null, null);
    }

    public static FieldOptions bytes() {
        return new FieldOptions(null, null, null, null, null);
    }

    public static FieldOptions date() {
        return new FieldOptions(null, null, null, null, null);
    }

    public FieldOptions indexed(boolean indexed) {
        return new FieldOptions(indexed ? true : null, store, fast, tokenizer, indexRecordOption);
    }

    public FieldOptions stored(boolean stored) {
        return new FieldOptions(index, stored ? true : null, fast, tokenizer, indexRecordOption);
    }

    public FieldOptions fast(boolean fast) {
        return new FieldOptions(index, store, fast ? true : null, tokenizer, indexRecordOption);
    }

    public FieldOptions tokenizer(String tokenizer) {
        return new FieldOptions(index, store, fast, tokenizer, indexRecordOption);
    }

    public FieldOptions indexRecordOption(String option) {
        return new FieldOptions(index, store, fast, tokenizer, option);
    }

}
