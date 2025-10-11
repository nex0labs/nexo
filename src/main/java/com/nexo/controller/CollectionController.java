package com.nexo.controller;

import com.nexo.annotation.Controller;
import com.nexo.annotation.HttpMethod;
import com.nexo.annotation.Route;
import com.nexo.dto.CollectionRequest;

@Controller("/collections")
public class CollectionController {

  @Route(path = "/", method = HttpMethod.POST)
  public CollectionRequest create(CollectionRequest collectionRequest) {
    System.out.println("CollectionController is working" + collectionRequest.toString());
    return collectionRequest;
  }
}
