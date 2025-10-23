package com.nexo.api.collection;

import com.nexo.server.annotation.Controller;
import com.nexo.server.annotation.HttpMethod;
import com.nexo.server.annotation.Route;
import jakarta.validation.Valid;

@Controller("/collections")
public class CollectionController {

  @Route(method = HttpMethod.POST)
  public CollectionRequest create(@Valid CollectionRequest collectionRequest) {
    if (collectionRequest.getFields() != null) {
      for (FieldRequest field : collectionRequest.getFields()) {
        field.validate();
      }
    }

    // TODO: Implement create collection logic
    return collectionRequest;
  }
}
