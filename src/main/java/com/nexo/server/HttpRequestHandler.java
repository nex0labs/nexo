package com.nexo.server;

import com.nexo.config.NexoConfig;
import com.nexo.controller.CollectionController;
import com.nexo.controller.HomeController;
import com.nexo.router.RequestRouter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);
  private static final HomeController HEALTH_CONTROLLER = new HomeController();
  private static final CollectionController COLLECTION_CONTROLLER = new CollectionController();
  private static final RequestRouter ROUTER = createRouter();

  private static RequestRouter createRouter() {
    RequestRouter router = new RequestRouter();
    router.registerController(HEALTH_CONTROLLER);
    router.registerController(COLLECTION_CONTROLLER);
    logger.info("Initialized routes: {}", router.getRegisteredRoutes());
    return router;
  }

  public HttpRequestHandler(NexoConfig config) {}

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
    if (logger.isDebugEnabled()) {
      logger.debug("Received {} request for {}", request.method(), request.uri());
    }
    ROUTER.route(ctx, request);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.error("Exception in HTTP handler", cause);
    ctx.close();
  }
}
