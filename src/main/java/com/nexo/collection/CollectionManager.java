package com.nexo.collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexo.config.NexoConfig;
import com.nexo.core.index.TantivyIndex;
import com.nexo.core.index.UsearchIndex;
import com.nexo.core.schema.SchemaBuilder;
import com.nexo.exception.CollectionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectionManager {

  // This should be the source of truth for collections
  private static volatile CollectionManager instance;
  private static final Object LOCK = new Object();

  private final Map<CollectionName, Collection> collections = new ConcurrentHashMap<>();

  /**
   * Maximum number of collections allowed per instance. This is a hard limit to prevent resource
   * exhaustion.
   */
  private static final int TOTAL_NUMBER_OF_COLLECTIONS = 100;

  private static final String COLLECTION_METADATA_FILE = "collection.json";
  private static final String KEYWORD_INDEX_DIR = "index";
  private static final String VECTOR_INDEX = "vectors.nexo";

  private final Path basePath;
  private final ObjectMapper objectMapper;

  private CollectionManager() {
    this(Path.of(NexoConfig.getInstance().getIndexPath()));
  }

  CollectionManager(Path basePath) {
    this.basePath = basePath;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    loadAllCollections();
  }

  public static CollectionManager getInstance() {
    if (instance == null) {
      synchronized (LOCK) {
        if (instance == null) {
          instance = new CollectionManager();
        }
      }
    }
    return instance;
  }

  private void loadAllCollections() {
    if (!Files.exists(basePath)) {
      log.info("Base path does not exist, no collections to load: {}", basePath);
      try {
        Files.createDirectories(basePath);
        log.info("Created base directory: {}", basePath);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot create base directory: " + basePath, e);
      }
      return;
    }

    int loadedCount = 0;
    int failedCount = 0;

    try (Stream<Path> dirStream = Files.list(basePath)) {
      for (Path collectionDir : dirStream.filter(Files::isDirectory).toList()) {
        try {
          String collectionId = collectionDir.getFileName().toString();
          Path keywordIndexPath = getKeywordIndexPath(collectionId);
          Path vectorIndexPath = getVectorIndexPath(collectionId);

          if (!Files.exists(keywordIndexPath)) {
            log.warn("Keyword index missing for collection: {}, skipping", collectionId);
            failedCount++;
            continue;
          }
          if (!Files.exists(vectorIndexPath)) {
            log.warn("Vector index missing for collection: {}, skipping", collectionId);
            failedCount++;
            continue;
          }

          CollectionMetadata metadata = loadCollectionMetadata(collectionDir);
          CollectionName name = CollectionName.of(metadata.getName());
          TantivyIndex tantivyIndex = new TantivyIndex(keywordIndexPath);
          UsearchIndex usearchIndex = new UsearchIndex();
          Collection collection = new Collection(metadata, tantivyIndex, usearchIndex);
          collections.put(name, collection);

          loadedCount++;
          log.info("Loaded collection: {} with ID: {}", name, collectionId);
        } catch (Exception e) {
          failedCount++;
          log.error("Failed to load collection from: {}", collectionDir, e);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Critical error loading collections from: " + basePath, e);
    }

    log.info(
        "Collection loading complete. Loaded: {}, Failed: {}, Total in memory: {}",
        loadedCount,
        failedCount,
        collections.size());

    if (loadedCount == 0 && failedCount > 0) {
      log.warn(
          "WARNING: No collections loaded successfully, but {} directories found", failedCount);
    }
  }

  public synchronized void createCollection(CollectionName name, SchemaBuilder schemaBuilder) {
    if (collectionExists(name)) {
      throw new CollectionException("Collection already exists: " + name);
    }
    if (schemaBuilder == null || schemaBuilder.getFields().isEmpty()) {
      throw new CollectionException("Schema must have at least one field");
    }
    if (collections.size() >= TOTAL_NUMBER_OF_COLLECTIONS) {
      throw new CollectionException(
          String.format(
              "Collection limit exceeded. Current: %d, Maximum: %d",
              collections.size(), TOTAL_NUMBER_OF_COLLECTIONS));
    }

    java.time.Instant now = java.time.Instant.now();
    String collectionId = generateCollectionId(name.toString());
    Path collectionBasePath = getCollectionBasePath(collectionId);
    Path keywordIndexPath = getKeywordIndexPath(collectionId);
    Path vectorIndexPath = getVectorIndexPath(collectionId);

    UsearchIndex usearchIndex = new UsearchIndex();
    TantivyIndex tantivyIndex = new TantivyIndex(keywordIndexPath);

    try {
      Files.createDirectories(collectionBasePath);

      boolean isKeywordIndexCreated = tantivyIndex.createIndex(schemaBuilder.toJson());
      if (!isKeywordIndexCreated) {
        cleanupCollectionDirectory(collectionBasePath);
        throw new CollectionException("Failed to create keyword index for collection: " + name);
      }

      boolean isVectorIndexCreated = usearchIndex.createIndex(vectorIndexPath.toString());
      if (!isVectorIndexCreated) {
        cleanupCollectionDirectory(collectionBasePath);
        throw new CollectionException("Failed to create vector index for collection: " + name);
      }

      CollectionMetadata metadata =
          CollectionMetadata.builder()
              .name(name.toString())
              .id(collectionId)
              .createdAt(now)
              .updatedAt(now)
              .fields(schemaBuilder.getFields())
              .status(CollectionStatus.OPEN)
              .build();

      saveCollectionMetadata(collectionBasePath, metadata);
      Collection collection = new Collection(metadata, tantivyIndex, usearchIndex);
      collections.put(name, collection);

      log.info(
          "Created new collection: {} with ID: {} at {}", name, collectionId, collectionBasePath);
      log.info("Keyword index: {}", keywordIndexPath);
      log.info("Vector index: {}", vectorIndexPath);
    } catch (CollectionException e) {
      throw e;
    } catch (JsonProcessingException e) {
      cleanupCollectionDirectory(collectionBasePath);
      log.error("Failed to create index for collection: {}", name, e);
      throw new CollectionException("Failed to serialize schema for collection: " + name, e);
    } catch (Exception e) {
      cleanupCollectionDirectory(collectionBasePath);
      log.error("Failed to create collection: {}", name, e);
      throw new CollectionException("Failed to create collection: " + name, e);
    }
  }

  private void cleanupCollectionDirectory(Path collectionPath) {
    if (collectionPath == null || !Files.exists(collectionPath)) {
      return;
    }

    try (Stream<Path> paths = Files.walk(collectionPath)) {
      paths
          .sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                  log.debug("Cleaned up: {}", path);
                } catch (IOException e) {
                  log.warn("Failed to delete during cleanup: {}", path, e);
                }
              });
      log.info("Cleaned up collection directory: {}", collectionPath);
    } catch (IOException e) {
      log.error("Failed to cleanup collection directory: {}", collectionPath, e);
    }
  }

  public Collection getCollection(CollectionName name) {
    Collection collection = collections.get(name);
    if (collection == null) {
      throw new CollectionException("Collection not found: " + name);
    }
    return collection;
  }

  public boolean collectionExists(CollectionName name) {
    return collections.containsKey(name);
  }

  public synchronized void deleteCollection(CollectionName name) {
    Collection collection = collections.remove(name);
    if (collection == null) {
      throw new CollectionException("Collection not found: " + name);
    }

    Path indexDir = getCollectionBasePath(collection.getMetadata().getId());
    cleanupCollectionDirectory(indexDir);
    log.info("Deleted collection: {}", name);
  }

  private String generateCollectionId(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Collection name cannot be null or empty");
    }
    String seed = name + java.time.Instant.now().toEpochMilli();
    byte[] seedBytes = seed.getBytes(StandardCharsets.UTF_8);
    String fullUuid = UUID.nameUUIDFromBytes(seedBytes).toString().replace("-", "");
    return fullUuid.substring(0, 9);
  }

  private void validateCollectionId(String collectionID) {
    if (collectionID == null || collectionID.trim().isEmpty()) {
      throw new IllegalArgumentException("Collection ID cannot be null or empty");
    }
    if (collectionID.contains("..")
        || collectionID.contains("/")
        || collectionID.contains("\\")
        || collectionID.contains(":")) {
      throw new SecurityException("Invalid collection ID: contains illegal characters");
    }
    if (collectionID.length() != 9) {
      throw new IllegalArgumentException("Collection ID must be exactly 9 characters");
    }
  }

  private Path getCollectionBasePath(String collectionID) {
    validateCollectionId(collectionID);
    Path resolved = basePath.resolve(collectionID).normalize().toAbsolutePath();
    if (!resolved.startsWith(basePath.normalize().toAbsolutePath())) {
      throw new SecurityException(
          "Path traversal attempt detected for collection ID: " + collectionID);
    }
    return resolved;
  }

  private Path getKeywordIndexPath(String collectionID) {
    return getCollectionBasePath(collectionID).resolve(KEYWORD_INDEX_DIR);
  }

  private Path getVectorIndexPath(String collectionID) {
    return getCollectionBasePath(collectionID).resolve(VECTOR_INDEX);
  }

  private void saveCollectionMetadata(Path collectionPath, CollectionMetadata metadata)
      throws IOException {
    Path metadataPath = collectionPath.resolve(COLLECTION_METADATA_FILE);
    objectMapper.writeValue(metadataPath.toFile(), metadata);
    log.info("Saved collection metadata to: {}", metadataPath);
  }

  private CollectionMetadata loadCollectionMetadata(Path collectionPath) throws IOException {
    Path metadataPath = collectionPath.resolve(COLLECTION_METADATA_FILE);
    if (!Files.exists(metadataPath)) {
      throw new IOException("Collection metadata file not found: " + metadataPath);
    }
    return objectMapper.readValue(metadataPath.toFile(), CollectionMetadata.class);
  }
}
