package com.nexo.collection;

import com.nexo.core.index.TantivyIndex;
import com.nexo.core.index.UsearchIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Collection {

  private final CollectionMetadata metadata;
  private final TantivyIndex tantivyIndex;
  private final UsearchIndex usearchIndex;
}
