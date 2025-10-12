package com.nexo.server;

import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.List;
import java.util.Map;

public class QueryParams {
  private final Map<String, List<String>> params;

  public QueryParams(String uri) {
    this.params = new QueryStringDecoder(uri).parameters();
  }

  public String get(String name) {
    List<String> values = params.get(name);
    return values != null && !values.isEmpty() ? values.get(0) : null;
  }

  public String getOrDefault(String name, String defaultValue) {
    String value = get(name);
    return value != null ? value : defaultValue;
  }

  public List<String> getAll(String name) {
    return params.getOrDefault(name, List.of());
  }

  @Override
  public String toString() {
    return params.toString();
  }
}
