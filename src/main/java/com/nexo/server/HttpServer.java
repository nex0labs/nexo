package com.nexo.server;

import com.nexo.config.NexoConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServer {

  private final NexoConfig config;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel serverChannel;

  public HttpServer(NexoConfig config) {
    this.config = config;
  }

  public void start() throws InterruptedException {
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup(config.getWorkerThreads());

    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 1024)
          .option(ChannelOption.SO_REUSEADDR, true)
          .option(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)
          .childOption(ChannelOption.SO_KEEPALIVE, true)
          .childOption(ChannelOption.TCP_NODELAY, true)
          .childOption(ChannelOption.SO_RCVBUF, 65536)
          .childOption(ChannelOption.SO_SNDBUF, 65536)
          .childOption(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)
          .childOption(
              ChannelOption.RCVBUF_ALLOCATOR, new io.netty.channel.AdaptiveRecvByteBufAllocator())
          .childOption(
              ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024))
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                  ChannelPipeline pipeline = ch.pipeline();

                  pipeline.addLast("decoder", new HttpRequestDecoder());
                  pipeline.addLast("encoder", new HttpResponseEncoder());
                  pipeline.addLast(
                      "aggregator", new HttpObjectAggregator(config.getMaxContentLength()));
                  pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
                  pipeline.addLast("handler", new HttpRequestHandler());
                }
              });

      ChannelFuture future = bootstrap.bind(config.getServerHost(), config.getServerPort()).sync();
      serverChannel = future.channel();

      log.info("HTTP server bound to {}:{}", config.getServerHost(), config.getServerPort());

    } catch (Exception e) {
      shutdown();
      throw e;
    }
  }

  public void stop() {
    try {
      if (serverChannel != null) {
        serverChannel.close().sync();
      }
    } catch (InterruptedException e) {
      log.warn("Interrupted while closing server channel", e);
      Thread.currentThread().interrupt();
    } finally {
      shutdown();
    }
  }

  private void shutdown() {
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }
  }
}
