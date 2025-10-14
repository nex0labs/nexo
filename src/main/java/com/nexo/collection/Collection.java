package com.nexo.collection;

import com.nexo.tantivy.IndexWriter;
import lombok.Getter;

@Getter
public class Collection {
  private final CollectionMetadata metadata;
  private final IndexWriter indexWriter;
  // TODO: IndexReader when read operations are implemented
  //  private final IndexReader indexReader;
  private final String indexPath;

  public Collection(String indexPath, CollectionMetadata metadata, IndexWriter indexWriter) {
    this.metadata = metadata;
    this.indexWriter = indexWriter;
    this.indexPath = indexPath;
  }

  public String getName() {
    return metadata.getName();
  }

  public CollectionStatus getStatus() {
    return metadata.getStatus();
  }

  public long getDocumentCount() {
    return metadata.getDocumentCount();
  }

  public long getSizeInBytes() {
    return metadata.getSizeInBytes();
  }
}
