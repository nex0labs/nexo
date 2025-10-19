package com.nexo.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;

public interface DocumentWriter extends AutoCloseable {

  void addDocument(Document doc) throws JsonProcessingException;

  void addDocuments(List<Document> docs) throws JsonProcessingException;

  void commit();

  @Override
  void close();
}
