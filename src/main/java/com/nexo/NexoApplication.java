package com.nexo;

import com.nexo.config.NexoConfig;
import com.nexo.monitor.MemoryMonitor;
import com.nexo.server.HttpServer;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexoApplication {
  private static final Logger logger = LoggerFactory.getLogger(NexoApplication.class);

  private final NexoConfig config;
  private final HttpServer httpServer;
  private final MemoryMonitor memoryMonitor;

  public NexoApplication(NexoConfig config) {
    this.config = config;
    this.httpServer = new HttpServer(config);
    this.memoryMonitor = new MemoryMonitor();
  }

  public void start() {
    try {
      logger.info("Starting Nexo server on port {}", config.getServerPort());
      httpServer.start();
      memoryMonitor.start();
      logger.info("Nexo server started successfully");
    } catch (Exception e) {
      logger.error("Failed to start Nexo server", e);
      throw new RuntimeException("Failed to start server", e);
    }
  }

  public void stop() {
    try {
      logger.info("Stopping Nexo server");
      memoryMonitor.stop();
      httpServer.stop();
      logger.info("Nexo server stopped");
    } catch (Exception e) {
      logger.error("Error stopping Nexo server", e);
    }
  }

  public static void main(String[] args) {
    try {
      logger.info("Initializing Nexo Search Engine...");

      //      logger.info("Loading native library...");
      //      LoadNativeLibrary.load();
      //      logger.info("Native library loaded successfully");

      String configPath = args.length > 0 ? args[0] : "config/nexo.yml";
      if (configPath.isBlank()) {
        logger.error("Configuration not found,make sure the nexo.yaml file is present");
        System.exit(1);
      }
      NexoConfig config = NexoConfig.load(Paths.get(configPath));

      NexoApplication app = new NexoApplication(config);

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    logger.info("Shutdown hook triggered");
                    app.stop();
                  }));

      app.start();
      Thread.currentThread().join();

    } catch (Exception e) {
      logger.error("Application startup failed", e);
      System.exit(1);
    }
  }
}
