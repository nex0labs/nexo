package com.nexo.schema;

import com.nexo.enums.FieldType;
import com.nexo.enums.RecordOption;
import com.nexo.enums.Tokenizer;

public class TextFieldBuilder {

  private final String name;
  private final TextFieldOptions options;

  public TextFieldBuilder(String name) {
    this.name = name;
    this.options = new TextFieldOptions();
    this.options.setStored(true);
    this.options.setFacet(false);
    this.options.setIndexing(
        new TextFieldOptions.Indexing(RecordOption.BASIC, true, Tokenizer.DEFAULT));
  }

  public TextFieldBuilder tokenizer(Tokenizer tokenizer) {
    TextFieldOptions.Indexing current = options.getIndexing();
    options.setIndexing(
        new TextFieldOptions.Indexing(current.getRecord(), current.isFieldNorms(), tokenizer));
    return this;
  }

  public TextFieldBuilder record(RecordOption record) {
    TextFieldOptions.Indexing current = options.getIndexing();
    options.setIndexing(
        new TextFieldOptions.Indexing(record, current.isFieldNorms(), current.getTokenizer()));
    return this;
  }

  public TextFieldBuilder stored(boolean stored) {
    options.setStored(stored);
    return this;
  }

  public TextFieldBuilder facet(boolean facet) {
    options.setFacet(facet);
    return this;
  }

  public TextFieldBuilder fieldNorms(boolean fieldNorms) {
    options.setFieldNorm(fieldNorms);
    TextFieldOptions.Indexing current = options.getIndexing();
    options.setIndexing(
        new TextFieldOptions.Indexing(current.getRecord(), fieldNorms, current.getTokenizer()));
    return this;
  }

  public TextFieldBuilder vector(boolean vector) {
    options.setVector(vector);
    return this;
  }

  public Field build() {
    return new Field(name, FieldType.TEXT, options);
  }
}
