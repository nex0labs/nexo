package com.nexo.testutil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TempDirUtil {

  /**
   * Creates a unique temporary directory for test isolation. Each call creates a new directory with
   * a unique name to prevent test interference when running in parallel.
   *
   * @return Path to the newly created unique temporary directory
   * @throws IOException if directory creation fails
   */
  public static Path createTempDir() throws IOException {
    return Files.createTempDirectory("nexo_test_");
  }

  public static void deleteRecursively(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }

    Files.walkFileTree(
        path,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }
}
