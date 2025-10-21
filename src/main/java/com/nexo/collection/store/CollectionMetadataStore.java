package com.nexo.collection.store;

import com.nexo.collection.CollectionMetadata;
import com.nexo.collection.CollectionName;
import java.util.List;
import java.util.Optional;

public interface CollectionMetadataStore {

  void save(CollectionMetadata metadata);

  Optional<CollectionMetadata> load(CollectionName name);

  void delete(CollectionName name);

  List<CollectionName> listCollections();

  boolean exists(CollectionName name);
}
