package com.nexo.server;

import io.netty.handler.codec.http.HttpMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutePattern {
  private final String pattern;
  private final HttpMethod method;
  private final Pattern regex;
  private final List<String> paramNames = new ArrayList<>();
  private final boolean isStatic;

  public RoutePattern(String pattern, HttpMethod method) {
    this.pattern = pattern;
    this.method = method;
    this.regex = compilePattern(pattern);
    this.isStatic = paramNames.isEmpty();
  }

  private Pattern compilePattern(String pattern) {
    if (pattern.equals("/")) {
      return Pattern.compile("^/$");
    }

    StringBuilder regex = new StringBuilder("^");
    for (String segment : pattern.split("/")) {
      if (segment.isEmpty()) continue;

      regex.append("/");
      if (segment.startsWith(":")) {
        paramNames.add(segment.substring(1));
        regex.append("([^/]+)");
      } else {
        regex.append(Pattern.quote(segment));
      }
    }
    return Pattern.compile(regex.append("$").toString());
  }

  public boolean matches(String path, HttpMethod requestMethod) {
    return this.method.equals(requestMethod) && regex.matcher(path).matches();
  }

  public boolean isStatic() {
    return isStatic;
  }

  public PathParams extractParams(String path) {
    Matcher matcher = regex.matcher(path);
    PathParams params = new PathParams();
    if (matcher.matches()) {
      for (int i = 0; i < paramNames.size(); i++) {
        params.put(paramNames.get(i), matcher.group(i + 1));
      }
    }
    return params;
  }

  @Override
  public String toString() {
    return method + " " + pattern;
  }
}
