package com.nexo.router;

import io.netty.handler.codec.http.HttpMethod;
import java.util.Objects;

public class RouteKey {
  private final String path;
  private final HttpMethod method;

  public RouteKey(String path, HttpMethod method) {
    this.path = path;
    this.method = method;
  }

  public String getPath() {
    return path;
  }

  public HttpMethod getMethod() {
    return method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RouteKey routeKey = (RouteKey) o;
    return Objects.equals(path, routeKey.path) && Objects.equals(method, routeKey.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, method);
  }

  @Override
  public String toString() {
    return method + " " + path;
  }
}
