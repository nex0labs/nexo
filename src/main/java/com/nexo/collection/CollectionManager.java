package com.nexo.collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexo.collection.store.FileMetadataStore;
import com.nexo.exception.CollectionException;
import com.nexo.tantivy.IndexWriter;
import com.nexo.tantivy.index.FSIndex;
import com.nexo.tantivy.schema.SchemaBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectionManager {

  // This should be the source of truth for collections
  private final Map<CollectionName, Collection> collections = new ConcurrentHashMap<>();
  private final FileMetadataStore fileMetadataStore;

  /**
   * Maximum number of collections allowed per instance. This is a hard limit to prevent resource
   * exhaustion.
   */
  private static final int TOTAL_NUMBER_OF_COLLECTIONS = 100;

  private final String basePath;

  public CollectionManager(FileMetadataStore fileMetadataStore, String basePath) {
    this.fileMetadataStore = fileMetadataStore;
    this.basePath = basePath;
    loadAllCollections();
  }

  private void loadAllCollections() {
    List<CollectionName> collectionNames = fileMetadataStore.listCollections();
    log.info("Loading {} existing collections", collectionNames.size());

    for (CollectionName name : collectionNames) {
      try {
        fileMetadataStore
            .load(name)
            .ifPresent(
                metadata -> {
                  String indexPath = getCollectionIndexPath(name, metadata.getId());
                  IndexWriter indexWriter = new IndexWriter(indexPath);
                  Collection collection = new Collection(indexPath, metadata, indexWriter);
                  collections.put(name, collection);
                  log.info("Loaded collection: {} with ID: {}", name, metadata.getId());
                });
      } catch (Exception e) {
        log.error("Failed to load collection: {}", name, e);
      }
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

    FSIndex directory = new FSIndex();
    try {
      java.time.Instant now = java.time.Instant.now();
      String collectionId = generateCollectionId(name.toString());
      String indexPath = getCollectionIndexPath(name, collectionId);
      boolean isCreated = directory.createIndex(indexPath, schemaBuilder.toJson());
      if (!isCreated) {
        throw new CollectionException("Failed to create index for collection: " + name);
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

      fileMetadataStore.save(metadata);
      IndexWriter indexWriter = new IndexWriter(indexPath);
      Collection collection = new Collection(indexPath, metadata, indexWriter);
      collections.put(name, collection);

      log.info("Created new collection: {} with ID: {}", name, collectionId);
    } catch (CollectionException e) {
      throw e;
    } catch (JsonProcessingException e) {
      log.error("Failed to create index for collection: {}", name, e);
      throw new CollectionException("Failed to serialize schema for collection: " + name, e);
    } catch (Exception e) {
      log.error("Failed to create collection: {}", name, e);
      throw new CollectionException("Failed to create collection: " + name, e);
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
    return collections.containsKey(name) || fileMetadataStore.exists(name);
  }

  public synchronized void deleteCollection(CollectionName name) {
    Collection collection = collections.remove(name);
    if (collection == null) {
      throw new CollectionException("Collection not found: " + name);
    }

    Path indexDir = Paths.get(getCollectionIndexPath(name, collection.getMetadata().getId()));
    try {
      if (Files.exists(indexDir)) {
        try (Stream<Path> paths = Files.walk(indexDir)) {
          paths
              .sorted(Comparator.reverseOrder())
              .forEach(
                  path -> {
                    try {
                      Files.delete(path);
                    } catch (IOException e) {
                      log.error("Failed to delete path: {}", path, e);
                    }
                  });
        }
      }
      fileMetadataStore.delete(name);
      log.info("Deleted collection: {}", name);
    } catch (CollectionException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to delete collection: {}", name, e);
      throw new CollectionException("Failed to delete collection: " + name, e);
    }
  }

  private String generateCollectionId(String name) {
    String seed = name + java.time.Instant.now().toEpochMilli();
    byte[] seedBytes = seed.getBytes(StandardCharsets.UTF_8);
    String fullUuid = UUID.nameUUIDFromBytes(seedBytes).toString().replace("-", "");
    return fullUuid.substring(0, 7);
  }

  private String getCollectionIndexPath(CollectionName name, String collectionID) {
    return Paths.get(basePath, name.toString(), collectionID).toString();
  }
}
