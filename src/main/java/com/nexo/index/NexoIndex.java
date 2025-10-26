package com.nexo.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexo.document.Document;
import java.util.List;

public interface NexoIndex extends AutoCloseable {

  void addDocument(Document doc) throws JsonProcessingException;

  void addDocuments(List<Document> docs) throws JsonProcessingException;

  void commit();

  @Override
  void close();
}
