package com.nexo.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexo.core.utils.LoadNativeLibrary;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexWriter implements AutoCloseable {

  private static final Logger logger = Logger.getLogger(IndexWriter.class.getName());

  static {
    try {
      LoadNativeLibrary.load();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(
          "Failed to load native library for IndexWriter: " + e.getMessage());
    }
  }

  private volatile long nativeHandle;
  private volatile boolean closed = false;

  public long getNativeHandle() {
    return nativeHandle;
  }

  private static native long writerNative(String indexPath);

  private static native void addDocumentNative(long nativeHandle, String documents);

  private static native void commitWriterNative(long nativeHandle);

  private static native void closeWriterNative(long nativeHandle);

  public IndexWriter(String path) {
    if (path == null || path.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }

    long handle = writerNative(path);
    if (handle == 0) {
      throw new IllegalStateException("Failed to create native IndexWriter for path: " + path);
    }
    this.nativeHandle = handle;
  }

  public void addDocument(Document doc) throws JsonProcessingException {
    Objects.requireNonNull(doc, "Document cannot be null");
    ensureNotClosed();

    try {
      addDocumentNative(this.nativeHandle, doc.toJson());
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to add document", e);
      throw e;
    }
  }

  public void commit() {
    ensureNotClosed();

    try {
      commitWriterNative(this.nativeHandle);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to commit", e);
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
        logger.log(Level.WARNING, "Error closing IndexWriter", e);
      } finally {
        closed = true;
      }
    }
  }

  private void ensureNotClosed() {
    if (closed) {
      throw new IllegalStateException("IndexWriter is already closed");
    }
  }
}
