package com.nexo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoadNativeLibrary {
  private static final Logger log = LoggerFactory.getLogger(LoadNativeLibrary.class);
  private static final String LIB_NAME = "tantivy4j";
  private static final AtomicBoolean loaded = new AtomicBoolean(false);
  private static volatile Throwable loadError = null;

  private LoadNativeLibrary() {}

  public static synchronized void load() {
    if (loaded.get()) {
      return;
    }

    if (loadError != null) {
      throw new RuntimeException("Native library failed to load previously", loadError);
    }

    try {
      OSInfo osInfo = detectOS();
      String debugSuffix = System.getenv("DEBUG_NATIVE") != null ? "-debug" : "";

      if (tryLoadFromSystem(LIB_NAME + debugSuffix)) {
        loaded.set(true);
        return;
      }
      tryLoadFromClasspath(LIB_NAME + debugSuffix, osInfo);
      loaded.set(true);
    } catch (Throwable e) {
      loadError = e;
      throw new RuntimeException("Failed to load native library: " + e.getMessage(), e);
    }
  }

  private static boolean tryLoadFromSystem(String name) {
    try {
      log.debug("Trying System.loadLibrary for {}", name);
      System.loadLibrary(name);
      log.info("Native library '{}' loaded from java.library.path", name);
      return true;
    } catch (UnsatisfiedLinkError e) {
      log.warn("Could not load '{}' from java.library.path: {}", name, e.getMessage());
      return false;
    }
  }

  private static void tryLoadFromClasspath(String name, OSInfo osInfo) {
    String resourcePath =
        String.format(
            "/native/%s-%s/%s%s%s",
            osInfo.dirName, osInfo.arch, osInfo.prefix, name, osInfo.suffix);

    Path tempFile = null;
    try (InputStream in = LoadNativeLibrary.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new RuntimeException("Native library not found in classpath: " + resourcePath);
      }

      tempFile = Files.createTempFile("tantivy4j_", osInfo.suffix);
      tempFile.toFile().deleteOnExit();

      try (OutputStream out = Files.newOutputStream(tempFile, StandardOpenOption.WRITE)) {
        in.transferTo(out);
      }

      System.load(tempFile.toAbsolutePath().toString());
      log.info("Native library '{}' loaded from classpath at {}", name, tempFile);

    } catch (IOException e) {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        } catch (IOException cleanupError) {
          log.warn("Failed to cleanup temp file: {}", tempFile, cleanupError);
        }
      }
      throw new RuntimeException(
          "Failed to extract and load native library from " + resourcePath, e);
    }
  }

  private static OSInfo detectOS() {
    String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    String arch = normalizeArch(System.getProperty("os.arch").toLowerCase(Locale.ENGLISH));

    if (osName.contains("mac") || osName.contains("darwin")) {
      return new OSInfo("lib", ".dylib", "mac", arch);
    } else if (osName.contains("linux")) {
      return new OSInfo("lib", ".so", "linux", arch);
    }
    throw new UnsupportedOperationException("Unsupported OS: " + osName + " (" + arch + ")");
  }

  private static String normalizeArch(String arch) {
    switch (arch) {
      case "x86_64":
      case "amd64":
        return "x86_64";
      case "aarch64":
      case "arm64":
        return "aarch64";
      case "x86":
      case "i386":
        return "x86";
      default:
        return arch;
    }
  }

  private record OSInfo(String prefix, String suffix, String dirName, String arch) {}
}
