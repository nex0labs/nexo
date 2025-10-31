package com.nexo.api.collection;

import com.nexo.server.annotation.Controller;
import com.nexo.server.annotation.HttpMethod;
import com.nexo.server.annotation.Route;
import jakarta.validation.Valid;

@Controller("/collections")
public class CollectionController {

  @Route(method = HttpMethod.POST)
  public RequestEntity<CollectionRequest> create(@Valid CollectionRequest collectionRequest) {
    if (collectionRequest.getFields() != null) {
      for (FieldRequest field : collectionRequest.getFields()) {
        field.validate();
      }
    }

    // TODO: Implement create collection logic

    // Example usage patterns:
    // Simple success response
    return RequestEntity.created(collectionRequest);

    // With custom headers
    // return RequestEntity.builder()
    //     .statusCode(201)
    //     .header("Location", "/collections/" + collectionRequest.getName())
    //     .body(collectionRequest);

    // Error responses
    // return RequestEntity.badRequest("Invalid collection configuration");
    // return RequestEntity.notFound();
  }
}
