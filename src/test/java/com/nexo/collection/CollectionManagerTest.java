package com.nexo.collection;

import static org.junit.jupiter.api.Assertions.*;

import com.nexo.collection.store.FileMetadataStore;
import com.nexo.core.schema.FieldFlag;
import com.nexo.core.schema.SchemaBuilder;
import com.nexo.exception.CollectionException;
import com.nexo.testutil.TempDirUtil;
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

  public static SchemaBuilder getTestSchema() {
    SchemaBuilder schemaBuilder = new SchemaBuilder();
    schemaBuilder
        .addTextField("id", "default", FieldFlag.INDEXED, FieldFlag.STORED)
        .addTextField("title", "default", FieldFlag.INDEXED, FieldFlag.STORED)
        .addTextField("content", "default", FieldFlag.INDEXED)
        .addTextField("url", "raw", FieldFlag.STORED);
    return schemaBuilder;
  }

  @Test
  void testCreateCollection() {
    CollectionName name = CollectionName.of("test-collection");
    collectionManager.createCollection(name, getTestSchema());

    assertTrue(collectionManager.collectionExists(name));
    assertNotNull(collectionManager.getCollection(name));
  }

  @Test
  void testCreateDuplicateCollection() {
    CollectionName name = CollectionName.of("test-collection");
    collectionManager.createCollection(name, getTestSchema());

    assertThrows(
        CollectionException.class, () -> collectionManager.createCollection(name, getTestSchema()));
  }

  @Test
  void testCreateCollectionWithNullSchema() {
    CollectionName name = CollectionName.of("test-collection");
    assertThrows(CollectionException.class, () -> collectionManager.createCollection(name, null));
  }
}
