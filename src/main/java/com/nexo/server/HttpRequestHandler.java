package com.nexo.server;

import com.nexo.api.collection.CollectionController;
import com.nexo.api.home.HomeController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final RequestRouter ROUTER = initRouter();

  private static RequestRouter initRouter() {
    RequestRouter router = new RequestRouter();
    router.registerController(new HomeController());
    router.registerController(new CollectionController());
    log.info("Registered routes: {}", router.getRegisteredRoutes());
    return router;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
    ROUTER.route(ctx, request);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("Request handling error", cause);
    ctx.close();
  }
}
