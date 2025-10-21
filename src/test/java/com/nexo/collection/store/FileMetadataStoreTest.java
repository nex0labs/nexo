package com.nexo.collection.store;

import static org.junit.jupiter.api.Assertions.*;

import com.nexo.collection.CollectionMetadata;
import com.nexo.collection.CollectionName;
import com.nexo.collection.CollectionStatus;
import com.nexo.testutil.TempDirUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileMetadataStoreTest {

  private Path tempDir;
  private FileMetadataStore store;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = TempDirUtil.createTempDir();
    store = new FileMetadataStore(tempDir);
  }

  @AfterEach
  void tearDown() throws IOException {
    if (tempDir != null) {
      TempDirUtil.deleteRecursively(tempDir);
    }
  }

  @Test
  void testSaveMetadata() {
    CollectionMetadata metadata = createTestMetadata("test-collection");
    store.save(metadata);
    Path metadataFile = tempDir.resolve("test-collection").resolve("collection.json");
    assertTrue(Files.exists(metadataFile));
  }

  @Test
  void testLoadMetadata() {
    CollectionMetadata metadata = createTestMetadata("test-collection");
    store.save(metadata);

    Optional<CollectionMetadata> loaded = store.load(CollectionName.of("test-collection"));
    assertTrue(loaded.isPresent());
    assertEquals("test-collection", loaded.get().getName());
    assertEquals(CollectionStatus.OPEN, loaded.get().getStatus());
  }

  @Test
  void testLoadNonExistentMetadata() {
    Optional<CollectionMetadata> loaded = store.load(CollectionName.of("non-existent"));
    assertFalse(loaded.isPresent());
  }

  @Test
  void testExistsMetadata() {
    CollectionMetadata metadata = createTestMetadata("test-collection");
    store.save(metadata);

    assertTrue(store.exists(CollectionName.of("test-collection")));
    assertFalse(store.exists(CollectionName.of("non-existent")));
  }

  @Test
  void testDeleteMetadata() {
    CollectionMetadata metadata = createTestMetadata("test-collection");
    store.save(metadata);
    assertTrue(store.exists(CollectionName.of("test-collection")));

    store.delete(CollectionName.of("test-collection"));
    assertFalse(store.exists(CollectionName.of("test-collection")));
  }

  @Test
  void testListCollections() {
    store.save(createTestMetadata("collection-1"));
    store.save(createTestMetadata("collection-2"));
    store.save(createTestMetadata("collection-3"));

    List<CollectionName> collections = store.listCollections();
    assertEquals(3, collections.size());
    assertTrue(collections.contains(CollectionName.of("collection-1")));
    assertTrue(collections.contains(CollectionName.of("collection-2")));
    assertTrue(collections.contains(CollectionName.of("collection-3")));
  }

  @Test
  void testListCollectionsEmpty() {
    List<CollectionName> collections = store.listCollections();
    assertEquals(0, collections.size());
  }

  private CollectionMetadata createTestMetadata(String name) {
    return CollectionMetadata.builder()
        .name(name)
        .id("test-id")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .documentCount(0)
        .status(CollectionStatus.OPEN)
        .sizeInBytes(0)
        .build();
  }
}
