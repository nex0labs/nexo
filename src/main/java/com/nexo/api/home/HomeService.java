package com.nexo.api.home;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class HomeService {

  private static final String VERSION_PROPERTIES = "version.properties";
  private static final String DEFAULT_VERSION = "unknown";

  private final String version;
  private final String artifactId;
  private final String groupId;
  private final String buildTimestamp;
  private final String tantivyVersion;

  public HomeService() {
    Properties properties = loadVersionProperties();

    this.version = properties.getProperty("version", DEFAULT_VERSION);
    this.artifactId = properties.getProperty("artifact.id", "nexo");
    this.groupId = properties.getProperty("group.id", "com.nexo");
    this.buildTimestamp = properties.getProperty("build.timestamp", "unknown");
    this.tantivyVersion = properties.getProperty("tantivy.version", DEFAULT_VERSION);

    log.info("Loaded version info: {} v{}", artifactId, version);
  }

  private Properties loadVersionProperties() {
    Properties properties = new Properties();

    try (InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES)) {
      if (inputStream == null) {
        log.warn("Version properties file not found: {}", VERSION_PROPERTIES);
        return properties;
      }

      properties.load(inputStream);
      log.debug("Successfully loaded version properties");

    } catch (IOException e) {
      log.error("Failed to load version properties", e);
    }

    return properties;
  }

  public String getFullVersion() {
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }
}
