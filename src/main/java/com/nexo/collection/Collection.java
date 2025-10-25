package com.nexo.collection;

import com.nexo.index.TantivyIndex;
import com.nexo.index.UsearchIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Collection {

  private final CollectionMetadata metadata;
  private final TantivyIndex tantivyIndex;
  private final UsearchIndex usearchIndex;
}
