package com.nexo;

import static com.nexo.config.NexoConfig.CONFIG_FILE_PATH;

import com.nexo.config.NexoConfig;
import com.nexo.monitor.MemoryMonitor;
import com.nexo.server.HttpServer;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NexoApplication {

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
      log.info("Starting Nexo server on port {}", config.getServerPort());
      httpServer.start();
      memoryMonitor.start();
      log.info("Nexo server started successfully");
    } catch (Exception e) {
      log.error("Failed to start Nexo server", e);
      throw new RuntimeException("Failed to start server", e);
    }
  }

  public void stop() {
    try {
      log.info("Stopping Nexo server");
      memoryMonitor.stop();
      httpServer.stop();
      log.info("Nexo server stopped");
    } catch (Exception e) {
      log.error("Error stopping Nexo server", e);
    }
  }

  public static void main(String[] args) {
    try {
      log.info("Initializing Nexo Search Engine...");

      String configPath = args.length > 0 ? args[0] : CONFIG_FILE_PATH;
      if (configPath.isBlank()) {
        log.error("Configuration not found,make sure the nexo.yaml file is present");
        System.exit(1);
      }
      NexoConfig config = NexoConfig.load(Paths.get(configPath));

      NexoApplication app = new NexoApplication(config);

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    log.info("Shutdown hook triggered");
                    app.stop();
                  }));

      app.start();
      Thread.currentThread().join();

    } catch (Exception e) {
      log.error("Application startup failed", e);
      System.exit(1);
    }
  }
}
