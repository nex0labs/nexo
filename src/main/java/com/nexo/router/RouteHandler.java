package com.nexo.router;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

@FunctionalInterface
public interface RouteHandler {
  Object handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception;
}
