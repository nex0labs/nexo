package com.nexo.server;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nexo.exception.NexoException;
import com.nexo.server.annotation.Controller;
import com.nexo.server.annotation.Route;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestRouter {

  private static final Logger logger = LoggerFactory.getLogger(RequestRouter.class);
  private static final ObjectMapper OBJECT_MAPPER = createOptimizedObjectMapper();

  private final Map<String, RouteHandler> staticRoutes = new HashMap<>();
  private final List<RouteEntry> dynamicRoutes = new ArrayList<>();

  private record RouteEntry(RoutePattern pattern, RouteHandler handler) {}

  @FunctionalInterface
  interface RouteHandler {

    Object handle(ChannelHandlerContext ctx, RequestContext requestContext) throws Exception;
  }

  private static ObjectMapper createOptimizedObjectMapper() {
    return new ObjectMapper()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private io.netty.handler.codec.http.HttpMethod convertHttpMethod(
      com.nexo.server.annotation.HttpMethod method) {
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

        RouteHandler handler = createHandler(controller, method);

        if (route.methods().length > 0) {
          for (com.nexo.server.annotation.HttpMethod httpMethod : route.methods()) {
            registerRoute(fullPath, httpMethod, handler);
          }
        } else {
          registerRoute(fullPath, route.method(), handler);
        }
      }
    }
  }

  private RouteHandler createHandler(Object controller, Method method) {
    Parameter[] parameters = method.getParameters();

    return (ctx, requestContext) -> {
      try {
        if (parameters.length == 0) {
          return method.invoke(controller);
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
          Class<?> paramType = parameters[i].getType();

          if (paramType == PathParams.class) {
            args[i] = requestContext.getPathParams();
          } else if (paramType == QueryParams.class) {
            args[i] = requestContext.getQueryParams();
          } else if (paramType == RequestContext.class) {
            args[i] = requestContext;
          } else {
            String body = requestContext.getRequest().content().toString(CharsetUtil.UTF_8);
            args[i] = OBJECT_MAPPER.readValue(body, paramType);
          }
        }

        return method.invoke(controller, args);
      } catch (Exception e) {
        Throwable cause =
            e instanceof java.lang.reflect.InvocationTargetException ? e.getCause() : e;

        if (cause instanceof NexoException) {
          throw (NexoException) cause;
        }

        logger.error("Error invoking controller method", cause);
        throw NexoException.internalError("Error invoking controller method", cause);
      }
    };
  }

  private void registerRoute(
      String path, com.nexo.server.annotation.HttpMethod httpMethod, RouteHandler handler) {
    io.netty.handler.codec.http.HttpMethod nettyMethod = convertHttpMethod(httpMethod);
    RoutePattern pattern = new RoutePattern(path, nettyMethod);

    if (pattern.isStatic()) {
      String routeKey = createRouteKey(nettyMethod, path);
      staticRoutes.put(routeKey, handler);
      logger.info("Registered static route: {}", pattern);
    } else {
      dynamicRoutes.add(new RouteEntry(pattern, handler));
      logger.info("Registered dynamic route: {}", pattern);
    }
  }

  private static String createRouteKey(HttpMethod method, String path) {
    return method.name() + ":" + path;
  }

  public void route(ChannelHandlerContext ctx, FullHttpRequest request) {
    String uri = request.uri();
    HttpMethod method = request.method();

    int queryStart = uri.indexOf('?');
    String path = queryStart >= 0 ? uri.substring(0, queryStart) : uri;

    if (path.length() > 1 && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    boolean prettyPrint = queryStart >= 0 && uri.indexOf("pretty=true", queryStart) >= 0;

    String routeKey = createRouteKey(method, path);
    RouteHandler staticHandler = staticRoutes.get(routeKey);

    if (staticHandler != null) {
      QueryParams queryParams = new QueryParams(uri);
      RequestContext requestContext = new RequestContext(request, PathParams.EMPTY, queryParams);

      try {
        Object result = staticHandler.handle(ctx, requestContext);
        sendSuccessResponse(ctx, result, prettyPrint);
        return;
      } catch (Exception e) {
        NexoException nexoException =
            e instanceof NexoException
                ? (NexoException) e
                : NexoException.internalError("Internal server error", e);

        logger.warn("Request handling error: {}", nexoException.getMessage(), e);
        sendError(ctx, nexoException, prettyPrint);
        return;
      }
    }

    QueryParams queryParams = new QueryParams(uri);

    for (RouteEntry entry : dynamicRoutes) {
      if (entry.pattern.matches(path, method)) {
        PathParams pathParams = entry.pattern.extractParams(path);
        RequestContext requestContext = new RequestContext(request, pathParams, queryParams);

        try {
          Object result = entry.handler.handle(ctx, requestContext);
          sendSuccessResponse(ctx, result, prettyPrint);
          return;
        } catch (Exception e) {
          NexoException nexoException =
              e instanceof NexoException
                  ? (NexoException) e
                  : NexoException.internalError("Internal server error", e);

          logger.warn("Request handling error: {}", nexoException.getMessage(), e);
          sendError(ctx, nexoException, prettyPrint);
          return;
        }
      }
    }

    NexoException notFound = NexoException.notFound("Endpoint not found: " + method + " " + path);
    sendError(ctx, notFound, prettyPrint);
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
      sendError(ctx, NexoException.internalError("Serialization error", e), prettyPrint);
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

  public List<RoutePattern> getRegisteredRoutes() {
    List<RoutePattern> allRoutes = new ArrayList<>();
    staticRoutes.forEach(
        (key, handler) -> {
          String[] parts = key.split(":", 2);
          allRoutes.add(new RoutePattern(parts[1], HttpMethod.valueOf(parts[0])));
        });
    allRoutes.addAll(dynamicRoutes.stream().map(entry -> entry.pattern).toList());
    return allRoutes;
  }
}
