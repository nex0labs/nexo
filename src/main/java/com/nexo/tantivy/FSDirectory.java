package com.nexo.tantivy;

import com.nexo.tantivy.utils.LoadNativeLibrary;

public class FSDirectory {

  static {
    try {
      LoadNativeLibrary.load();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(
          "Failed to load native library for FSDirectory: " + e.getMessage());
    }
  }

  private native boolean createIndexNative(String index_Path, String schema);

  protected native boolean deleteIndexNative(String indexPath);

  protected native boolean indexExistsNative(String indexPath);

  public boolean createIndex(String indexPath, String schema) {
    if (indexPath == null || indexPath.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }
    if (schema == null || schema.trim().isEmpty()) {
      throw new IllegalArgumentException("Schema cannot be null or empty");
    }

    try {
      return createIndexNative(indexPath, schema);
    } catch (Exception e) {
      throw new RuntimeException("Error creating index: " + e.getMessage(), e);
    }
  }

  public boolean deleteIndex(String indexPath) {
    if (indexPath == null || indexPath.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }

    try {
      return deleteIndexNative(indexPath);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting index: " + e.getMessage(), e);
    }
  }

  public boolean indexExists(String indexPath) {
    if (indexPath == null || indexPath.trim().isEmpty()) {
      throw new IllegalArgumentException("Index path cannot be null or empty");
    }

    try {
      return indexExistsNative(indexPath);
    } catch (Exception e) {
      throw new RuntimeException("Error checking if index exists: " + e.getMessage(), e);
    }
  }
}
