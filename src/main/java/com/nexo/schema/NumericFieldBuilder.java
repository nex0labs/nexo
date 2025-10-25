package com.nexo.schema;

import com.nexo.enums.FieldType;
import com.nexo.enums.PrecisionType;

// This has both numeric and date field options for date we have precision option
public class NumericFieldBuilder {
  private final String name;
  private final FieldType type;
  private final DefaultOptions options;

  public NumericFieldBuilder(String name, FieldType type) {
    this.name = name;
    this.type = type;
    this.options = new DefaultOptions();
    this.options.setIndexed(false);
    this.options.setStored(true);
    this.options.setFacet(true);
    this.options.setFieldNorm(false);
    this.options.setPrecision(null);
  }

  public NumericFieldBuilder indexed(boolean indexed) {
    options.setIndexed(indexed);
    return this;
  }

  public NumericFieldBuilder stored(boolean stored) {
    options.setStored(stored);
    return this;
  }

  public NumericFieldBuilder fast(boolean fast) {
    options.setFacet(fast);
    return this;
  }

  public NumericFieldBuilder facet(boolean facet) {
    options.setFacet(facet);
    return this;
  }

  public NumericFieldBuilder fieldNorms(boolean fieldNorms) {
    options.setFieldNorm(fieldNorms);
    return this;
  }

  public NumericFieldBuilder precision(PrecisionType precision) {
    options.setPrecision(precision);
    return this;
  }

  public Field build() {
    return new Field(name, type, options);
  }
}
