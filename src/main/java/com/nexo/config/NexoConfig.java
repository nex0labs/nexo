package com.nexo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class NexoConfig {
  private static final Logger logger = LoggerFactory.getLogger(NexoConfig.class);

  private String serverHost = "0.0.0.0";
  private int serverPort = 9200;
  private int workerThreads = Runtime.getRuntime().availableProcessors();
  private int searchThreads = Runtime.getRuntime().availableProcessors();
  private int maxContentLength = 1048576;
  private String indexPath = "data/index";
  private String clusterName = "nexo-cluster";
  private String nodeName = "nexo-node-1";

  public static NexoConfig load(Path configPath) {
    NexoConfig config = new NexoConfig();

    if (Files.exists(configPath)) {
      try {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        config = mapper.readValue(configPath.toFile(), NexoConfig.class);
        logger.info("Configuration loaded from: {}", configPath);
      } catch (IOException e) {
        logger.warn("Failed to load configuration from {}, using defaults", configPath, e);
      }
    } else {
      logger.info("Configuration file not found at {}, using defaults", configPath);
    }

    config.validate();
    return config;
  }

  private void validate() {
    if (serverPort < 1 || serverPort > 65535) {
      throw new IllegalArgumentException("Server port must be between 1 and 65535");
    }
    if (workerThreads < 1) {
      workerThreads = Runtime.getRuntime().availableProcessors();
    }
    if (searchThreads < 1) {
      searchThreads = Runtime.getRuntime().availableProcessors();
    }
    if (maxContentLength < 1024) {
      maxContentLength = 1024;
    }

    logger.info(
        "Configuration validated: host={}, port={}, workers={}, searchThreads={}",
        serverHost,
        serverPort,
        workerThreads,
        searchThreads);
  }

  @Override
  public String toString() {
    return "NexoConfig{"
        + "serverHost='"
        + serverHost
        + '\''
        + ", serverPort="
        + serverPort
        + ", workerThreads="
        + workerThreads
        + ", searchThreads="
        + searchThreads
        + ", maxContentLength="
        + maxContentLength
        + ", indexPath='"
        + indexPath
        + '\''
        + ", clusterName='"
        + clusterName
        + '\''
        + ", nodeName='"
        + nodeName
        + '\''
        + '}';
  }
}
