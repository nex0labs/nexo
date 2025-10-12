package com.nexo.api.collection;

import com.nexo.server.annotation.Controller;
import com.nexo.server.annotation.HttpMethod;
import com.nexo.server.annotation.Route;

@Controller("/collections")
public class CollectionController {

  @Route(method = HttpMethod.POST)
  public CollectionRequest create(CollectionRequest collectionRequest) {
    // TODO: Implement create collection logic
    return collectionRequest;
  }
}
