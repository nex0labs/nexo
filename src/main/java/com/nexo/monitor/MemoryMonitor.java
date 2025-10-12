package com.nexo.monitor;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.jmx.JmxMeterRegistry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryMonitor {
  private static final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);
  private static final long KB = 1024;
  private static final long MB = 1024 * 1024;
  private static final long GB = 1024 * 1024 * 1024;

  private final ScheduledExecutorService scheduler;
  private final int intervalSeconds;
  private final JmxMeterRegistry meterRegistry;

  public MemoryMonitor(int intervalSeconds) {
    this.intervalSeconds = intervalSeconds;
    this.scheduler =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "memory-monitor");
              t.setDaemon(true);
              return t;
            });
    this.meterRegistry = new JmxMeterRegistry(s -> null, Clock.SYSTEM);

    // Register JVM metrics with Micrometer
    new JvmMemoryMetrics().bindTo(meterRegistry);
    new JvmGcMetrics().bindTo(meterRegistry);
    new JvmThreadMetrics().bindTo(meterRegistry);
    new ProcessorMetrics().bindTo(meterRegistry);

    Metrics.addRegistry(meterRegistry);
  }

  public MemoryMonitor() {
    this(30);
  }

  public void start() {
    logger.info("Starting memory monitor with {}s interval", intervalSeconds);
    scheduler.scheduleAtFixedRate(
        this::logMemoryStats, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
  }

  public void stop() {
    logger.info("Stopping memory monitor");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    } finally {
      meterRegistry.close();
    }
  }

  private void logMemoryStats() {
    if (!logger.isInfoEnabled()) {
      return;
    }

    try {
      long heapUsed =
          (long) meterRegistry.get("jvm.memory.used").tag("area", "heap").gauge().value();
      long heapMax = (long) meterRegistry.get("jvm.memory.max").tag("area", "heap").gauge().value();
      long nonHeapUsed =
          (long) meterRegistry.get("jvm.memory.used").tag("area", "nonheap").gauge().value();
      int threadCount = (int) meterRegistry.get("jvm.threads.live").gauge().value();

      String cpuInfo = "N/A";
      try {
        double cpuUsage = meterRegistry.get("system.cpu.usage").gauge().value() * 100;
        if (!Double.isNaN(cpuUsage) && cpuUsage >= 0) {
          cpuInfo = String.format("%.1f%%", cpuUsage);
        }
      } catch (Exception cpuEx) {
        // CPU metric not available
      }

      logger.info(
          "Memory Stats: heap=[used={}, max={}] nonHeap=[used={}] threads={} cpu={}",
          formatBytes(heapUsed),
          formatBytes(heapMax),
          formatBytes(nonHeapUsed),
          threadCount,
          cpuInfo);
    } catch (Exception e) {
      logger.warn("Error collecting memory statistics", e);
    }
  }

  private String formatBytes(long bytes) {
    if (bytes < KB) return bytes + "B";
    if (bytes < MB) return String.format("%.1fKB", bytes / (double) KB);
    if (bytes < GB) return String.format("%.1fMB", bytes / (double) MB);
    return String.format("%.1fGB", bytes / (double) GB);
  }
}
