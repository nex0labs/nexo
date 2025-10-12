package com.nexo.server;

import io.netty.handler.codec.http.FullHttpRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(of = {"pathParams", "queryParams"})
public class RequestContext {
  private final FullHttpRequest request;
  private final PathParams pathParams;
  private final QueryParams queryParams;
}
