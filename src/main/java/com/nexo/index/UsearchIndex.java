package com.nexo.index;

import cloud.unum.usearch.Index;
import com.nexo.config.NexoConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsearchIndex {

  private final NexoConfig.VectorIndexConfig config;
  private Index index;
  private Path indexPath;

  public UsearchIndex(NexoConfig.VectorIndexConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("Vector index config cannot be null");
    }
    this.config = config;
  }

  public UsearchIndex() {
    this(NexoConfig.getInstance().getVectorIndex());
  }

  public boolean createIndex(String indexPathStr) {
    if (indexPathStr == null || indexPathStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }

    Index vectorIndex = null;
    try {
      this.indexPath = Paths.get(indexPathStr);
      Files.createDirectories(this.indexPath.getParent());

      vectorIndex =
          new Index.Config()
              .dimensions(config.getDimension())
              .metric(config.getMetric())
              .quantization(config.getQuantization())
              .connectivity(config.getConnectivity())
              .expansion_add(config.getExpansionAdd())
              .expansion_search(config.getExpansionSearch())
              .build();

      vectorIndex.save(indexPathStr);
      log.info("Created USearch index at: {}", indexPathStr);

      this.index = vectorIndex;
      vectorIndex = null;
      return true;
    } catch (Exception e) {
      log.error("Error creating USearch index: {}", e.getMessage(), e);
      return false;
    } finally {
      if (vectorIndex != null) {
        try {
          vectorIndex.close();
        } catch (Exception e) {
          log.warn("Error closing temporary index: {}", e.getMessage());
        }
      }
    }
  }

  public void open(String indexPathStr) throws Exception {
    if (indexPathStr == null || indexPathStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }

    this.indexPath = Paths.get(indexPathStr);
    if (!Files.exists(this.indexPath)) {
      throw new IllegalArgumentException("Index does not exist at: " + indexPathStr);
    }

    this.index =
        new Index.Config()
            .dimensions(config.getDimension())
            .metric(config.getMetric())
            .quantization(config.getQuantization())
            .connectivity(config.getConnectivity())
            .expansion_add(config.getExpansionAdd())
            .expansion_search(config.getExpansionSearch())
            .build();

    this.index.load(indexPathStr);
    log.info("Opened USearch index from: {}", indexPathStr);
  }

  public void close() throws Exception {
    if (index != null) {
      try {
        index.save(indexPath.toString());
        index.close();
        log.info("Closed USearch index at: {}", indexPath);
      } finally {
        index = null;
      }
    }
  }
}
