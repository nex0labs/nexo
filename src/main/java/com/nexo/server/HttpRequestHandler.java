package com.nexo.server;

import com.nexo.api.collection.CollectionController;
import com.nexo.api.home.HomeController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);
  private static final RequestRouter ROUTER = initRouter();

  private static RequestRouter initRouter() {
    RequestRouter router = new RequestRouter();
    router.registerController(new HomeController());
    router.registerController(new CollectionController());
    logger.info("Registered routes: {}", router.getRegisteredRoutes());
    return router;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
    ROUTER.route(ctx, request);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.error("Request handling error", cause);
    ctx.close();
  }
}
