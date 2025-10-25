package com.nexo.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexo.enums.PrecisionType;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DefaultOptions {

  //
  //    {
  //        "name": "_timestamp",
  //        "type": "date",
  //        "options": {
  //        "indexed": true,
  //            "fieldnorms": true,
  //            "fast": true,
  //            "stored": true,
  //            "precision": "seconds"
  //    }
  //    },

  private Boolean indexed = true;

  @JsonProperty("fieldnorms")
  private Boolean fieldNorm = true;

  @JsonProperty("fast")
  private boolean facet = true;

  private boolean stored = false;
  private PrecisionType precision = PrecisionType.SECONDS;
}
