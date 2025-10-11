package com.nexo.router;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nexo.annotation.Controller;
import com.nexo.annotation.Route;
import com.nexo.exception.InternalServerException;
import com.nexo.exception.NexoException;
import com.nexo.exception.NotFoundException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestRouter {
  private static final Logger logger = LoggerFactory.getLogger(RequestRouter.class);
  private static final ObjectMapper OBJECT_MAPPER = createOptimizedObjectMapper();

  private final Map<RouteKey, RouteHandler> routes = new HashMap<>();

  private static ObjectMapper createOptimizedObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  private io.netty.handler.codec.http.HttpMethod convertHttpMethod(
      com.nexo.annotation.HttpMethod method) {
    return switch (method) {
      case GET -> HttpMethod.GET;
      case POST -> HttpMethod.POST;
      case PUT -> HttpMethod.PUT;
      case DELETE -> HttpMethod.DELETE;
      case PATCH -> HttpMethod.PATCH;
      case HEAD -> HttpMethod.HEAD;
      case OPTIONS -> HttpMethod.OPTIONS;
      case TRACE -> HttpMethod.TRACE;
    };
  }

  public void registerController(Object controller) {
    Class<?> controllerClass = controller.getClass();

    if (!controllerClass.isAnnotationPresent(Controller.class)) {
      logger.warn("Class {} is not annotated with @Controller", controllerClass.getName());
      return;
    }

    String basePath = controllerClass.getAnnotation(Controller.class).value();

    for (Method method : controllerClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Route.class)) {
        Route route = method.getAnnotation(Route.class);
        String fullPath = basePath + route.path();

        RouteHandler handler =
            (ctx, request) -> {
              try {
                Class<?>[] paramTypes = method.getParameterTypes();

                if (paramTypes.length == 0) {
                  return method.invoke(controller);
                } else if (paramTypes.length == 1) {
                  String body = request.content().toString(CharsetUtil.UTF_8);
                  Object param = OBJECT_MAPPER.readValue(body, paramTypes[0]);
                  return method.invoke(controller, param);
                } else {
                  throw new InternalServerException(
                      "Controller methods can have at most 1 parameter");
                }
              } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                  throw (RuntimeException) cause;
                }
                throw new InternalServerException("Error invoking controller method", cause);
              } catch (Exception e) {
                logger.error("Error invoking controller method", e);
                throw new InternalServerException("Error invoking controller method", e);
              }
            };

        if (route.methods().length > 0) {
          for (com.nexo.annotation.HttpMethod httpMethod : route.methods()) {
            registerRoute(fullPath, httpMethod, handler);
          }
        } else {
          registerRoute(fullPath, route.method(), handler);
        }
      }
    }
  }

  private void registerRoute(
      String path, com.nexo.annotation.HttpMethod httpMethod, RouteHandler handler) {
    io.netty.handler.codec.http.HttpMethod nettyMethod = convertHttpMethod(httpMethod);
    RouteKey key = new RouteKey(path, nettyMethod);
    routes.put(key, handler);
    logger.info("Registered route: {}", key);
  }

  public void route(ChannelHandlerContext ctx, FullHttpRequest request) {
    String uri = request.uri();
    HttpMethod method = request.method();

    String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

    RouteKey key = new RouteKey(path, method);
    RouteHandler handler = routes.get(key);

    if (handler != null) {
      try {
        Object result = handler.handle(ctx, request);
        sendSuccessResponse(ctx, result, shouldPrettyPrint(uri));
      } catch (NexoException e) {
        logger.warn("Nexo exception: {}", e.getMessage());
        sendError(ctx, e, shouldPrettyPrint(uri));
      } catch (Exception e) {
        logger.error("Unexpected error handling request", e);
        NexoException nexoException = new InternalServerException("Internal server error", e);
        sendError(ctx, nexoException, shouldPrettyPrint(uri));
      }
    } else {
      NotFoundException notFound = new NotFoundException("Endpoint not found");
      sendError(ctx, notFound, shouldPrettyPrint(uri));
    }
  }

  private boolean shouldPrettyPrint(String uri) {
    return uri.contains("pretty=true");
  }

  private void sendSuccessResponse(ChannelHandlerContext ctx, Object content, boolean prettyPrint) {
    try {
      String json =
          prettyPrint
              ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(content)
              : OBJECT_MAPPER.writeValueAsString(content);
      FullHttpResponse response =
          new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));

      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

      ctx.writeAndFlush(response);
    } catch (Exception e) {
      logger.error("Error serializing response", e);
      sendError(ctx, new InternalServerException("Serialization error", e), prettyPrint);
    }
  }

  private void sendError(ChannelHandlerContext ctx, NexoException exception, boolean prettyPrint) {
    Map<String, Object> error =
        Map.of(
            "error", true,
            "message", exception.getMessage(),
            "status", exception.getStatus().code(),
            "errorCode", exception.getErrorCode(),
            "timestamp", System.currentTimeMillis());

    try {
      String json =
          prettyPrint
              ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(error)
              : OBJECT_MAPPER.writeValueAsString(error);
      FullHttpResponse response =
          new DefaultFullHttpResponse(
              HTTP_1_1, exception.getStatus(), Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));

      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    } catch (Exception e) {
      logger.error("Error sending error response", e);
      ctx.close();
    }
  }

  public Set<RouteKey> getRegisteredRoutes() {
    return routes.keySet();
  }
}
