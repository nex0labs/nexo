package com.nexo.collection;

import static org.junit.jupiter.api.Assertions.*;

import com.nexo.collection.store.FileMetadataStore;
import com.nexo.exception.CollectionException;
import com.nexo.testutil.TempDirUtil;
import com.nexo.testutil.TestSchemaUtils;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionManagerTest {

  private Path tempDir;
  private FileMetadataStore store;
  private CollectionManager collectionManager;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = TempDirUtil.createTempDir();
    store = new FileMetadataStore(tempDir);
    collectionManager = new CollectionManager(tempDir);
  }

  @AfterEach
  void tearDown() throws IOException {
    if (tempDir != null) {
      TempDirUtil.deleteRecursively(tempDir);
    }
  }

  @Test
  void testCreateCollection() {
    CollectionName name = CollectionName.of("test-collection");
    collectionManager.createCollection(name, TestSchemaUtils.getTestSchema());

    assertTrue(collectionManager.collectionExists(name));
    assertNotNull(collectionManager.getCollection(name));
  }

  @Test
  void testCreateDuplicateCollection() {
    CollectionName name = CollectionName.of("test-collection");
    collectionManager.createCollection(name, TestSchemaUtils.getTestSchema());

    assertThrows(
        CollectionException.class,
        () -> collectionManager.createCollection(name, TestSchemaUtils.getTestSchema()));
  }

  @Test
  void testCreateCollectionWithNullSchema() {
    CollectionName name = CollectionName.of("test-collection");
    assertThrows(CollectionException.class, () -> collectionManager.createCollection(name, null));
  }
}
