package com.nexo.server;

import java.util.HashMap;
import java.util.Map;

public class PathParams {
  public static final PathParams EMPTY = new PathParams();

  private final Map<String, String> params = new HashMap<>();

  public String get(String name) {
    return params.get(name);
  }

  public String getOrDefault(String name, String defaultValue) {
    return params.getOrDefault(name, defaultValue);
  }

  void put(String name, String value) {
    params.put(name, value);
  }

  @Override
  public String toString() {
    return params.toString();
  }
}
