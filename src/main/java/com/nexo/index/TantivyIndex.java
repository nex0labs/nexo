package com.nexo.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexo.document.Document;
import com.nexo.utils.LoadNativeLibrary;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class TantivyIndex implements NexoIndex {

  static {
    try {
      LoadNativeLibrary.load();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(
          "Failed to load native library for TantivyIndex: " + e.getMessage());
    }
  }

  private volatile long nativeHandle = 0L;
  private volatile boolean closed = false;
  private final Path indexPath;

  public TantivyIndex(Path indexPath) {
    String path = indexPath != null ? indexPath.toString() : null;
    if (path == null || path.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }
    this.indexPath = indexPath;
  }

  public long getIndexWriter() {
    if (this.nativeHandle != 0) {
      return this.nativeHandle;
    }

    synchronized (this) {
      if (this.nativeHandle != 0) {
        return this.nativeHandle;
      }

      String path = this.indexPath.toString();
      try {
        long handle = writerNative(path);
        if (handle == 0) {
          throw new IllegalStateException(
              "Failed to create native TantivyIndex writer for path: " + path);
        }
        this.nativeHandle = handle;
      } catch (Exception e) {
        log.error("Failed to open index writer at path: {}", path, e);
        throw new IllegalStateException(
            "Cannot open index writer. Ensure index exists at path: " + path, e);
      }
      return this.nativeHandle;
    }
  }

  public boolean createIndex(String schema) {
    if (schema == null || schema.trim().isEmpty()) {
      throw new IllegalArgumentException("Schema cannot be null or empty");
    }
    try {
      return createIndexNative(indexPath.toString(), schema);
    } catch (Exception e) {
      throw new RuntimeException("Error creating index: " + e.getMessage(), e);
    }
  }

  @Override
  public void addDocument(Document doc) throws JsonProcessingException {
    Objects.requireNonNull(doc, "Document cannot be null");
    ensureNotClosed();

    long handle = getIndexWriter();
    try {
      addDocumentNative(handle, doc.toJsonString(false));
    } catch (Exception e) {
      log.error("Failed to add document", e);
      throw e;
    }
  }

  @Override
  public void addDocuments(List<Document> docs) throws JsonProcessingException {
    if (docs == null || docs.isEmpty()) {
      log.debug("No documents to add");
      return;
    }

    ensureNotClosed();
    long handle = getIndexWriter();

    try {
      for (Document doc : docs) {
        Objects.requireNonNull(doc, "Document cannot be null");
        addDocumentNative(handle, doc.toJsonString(false));
      }
      log.debug("Added {} documents", docs.size());
    } catch (Exception e) {
      log.error("Failed to add documents batch", e);
      throw e;
    }
  }

  public void commit() {
    ensureNotClosed();

    long handle = getIndexWriter();
    try {
      commitWriterNative(handle);
    } catch (Exception e) {
      log.error("Failed to commit", e);
      throw new RuntimeException("Commit operation failed", e);
    }
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }

    synchronized (this) {
      if (closed) {
        return;
      }

      try {
        if (this.nativeHandle != 0) {
          closeWriterNative(this.nativeHandle);
          this.nativeHandle = 0;
        }
      } catch (Exception e) {
        log.warn("Error closing TantivyIndex", e);
      } finally {
        closed = true;
      }
    }
  }

  private void ensureNotClosed() {
    if (closed) {
      throw new IllegalStateException("TantivyIndex is already closed");
    }
  }

  private static native boolean createIndexNative(String indexPath, String schema);

  private static native long writerNative(String indexPath);

  private static native void addDocumentNative(long nativeHandle, String documents);

  private static native void commitWriterNative(long nativeHandle);

  private static native void closeWriterNative(long nativeHandle);
}
