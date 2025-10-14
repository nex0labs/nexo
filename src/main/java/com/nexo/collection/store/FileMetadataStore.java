package com.nexo.collection.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexo.collection.CollectionMetadata;
import com.nexo.collection.CollectionName;
import com.nexo.exception.ErrorType;
import com.nexo.exception.NexoException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMetadataStore implements CollectionMetadataStore {

  private static final String METADATA_FILE = "collection.json";

  private final Path basePath;
  private final ObjectMapper objectMapper;
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  public FileMetadataStore(String basePath) {
    this.basePath = Paths.get(basePath);
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    initializeStorage();
  }

  private void initializeStorage() {
    try {
      Files.createDirectories(basePath);
      log.info("Metadata storage initialized at: {}", basePath);
    } catch (IOException e) {
      throw new NexoException(ErrorType.INTERNAL_ERROR, "Failed to initialize metadata storage", e);
    }
  }

  @Override
  public void save(CollectionMetadata metadata) {
    lock.writeLock().lock();
    try {
      Path collectionPath = basePath.resolve(metadata.getName());
      Path metadataFile = collectionPath.resolve(METADATA_FILE);

      Files.createDirectories(collectionPath);
      objectMapper.writeValue(metadataFile.toFile(), metadata);
      log.info("Saved metadata for collection: {}", metadata.getName());
    } catch (IOException e) {
      throw new NexoException(
          ErrorType.INTERNAL_ERROR,
          "Failed to save metadata for collection: " + metadata.getName(),
          e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Optional<CollectionMetadata> load(CollectionName name) {
    lock.readLock().lock();
    try {
      Path metadataFile = basePath.resolve(name.toString()).resolve(METADATA_FILE);

      if (!Files.exists(metadataFile)) {
        return Optional.empty();
      }

      CollectionMetadata metadata =
          objectMapper.readValue(metadataFile.toFile(), CollectionMetadata.class);
      log.info("Loaded metadata for collection: {}", name);
      return Optional.of(metadata);
    } catch (IOException e) {
      throw new NexoException(
          ErrorType.INTERNAL_ERROR, "Failed to load metadata for collection: " + name, e);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void delete(CollectionName name) {
    lock.writeLock().lock();
    try {
      Path collectionPath = basePath.resolve(name.toString());

      if (Files.exists(collectionPath)) {
        try (var paths = Files.walk(collectionPath)) {
          paths
              .sorted((a, b) -> b.compareTo(a))
              .forEach(
                  path -> {
                    try {
                      Files.delete(path);
                    } catch (IOException e) {
                      log.error("Failed to delete: {}", path, e);
                    }
                  });
        }
        log.info("Deleted metadata for collection: {}", name);
      }
    } catch (IOException e) {
      throw new NexoException(
          ErrorType.INTERNAL_ERROR, "Failed to delete metadata for collection: " + name, e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public List<CollectionName> listCollections() {
    lock.readLock().lock();
    try (var paths = Files.list(basePath)) {
      return paths
          .filter(Files::isDirectory)
          .map(Path::getFileName)
          .map(Path::toString)
          .map(CollectionName::of)
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new NexoException(ErrorType.INTERNAL_ERROR, "Failed to list collections", e);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean exists(CollectionName name) {
    lock.readLock().lock();
    try {
      Path metadataFile = basePath.resolve(name.toString()).resolve(METADATA_FILE);
      return Files.exists(metadataFile);
    } finally {
      lock.readLock().unlock();
    }
  }
}
