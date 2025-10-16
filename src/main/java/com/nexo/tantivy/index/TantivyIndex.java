package com.nexo.tantivy.index;

public interface TantivyIndex {

  boolean createIndex(String indexPath, String schemaJson);

  boolean deleteIndex(String indexPath);

  boolean indexExists(String indexPath);
}
