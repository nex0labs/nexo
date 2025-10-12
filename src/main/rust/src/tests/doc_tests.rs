#[cfg(test)]
mod tests {
    use crate::create_index;
    use crate::doc_writer::DocumentWriter;
    use crate::tests::test_utils::{get_temp_path, DOC_TEST_SCHEMA};
    use serial_test::serial;

    #[test]
    #[serial(index)]
    fn test_add_document_basic() {
        let (_temp_dir, index_path) = get_temp_path("test_index_basic");
        create_index(&index_path, &DOC_TEST_SCHEMA).expect("Failed to create index");

        let mut doc_writer =
            DocumentWriter::new(&index_path).expect("Failed to open index");

        let doc_json = r#"{
            "id": 1,
            "title": "Test Document",
            "content": "This is a test.",
            "url": "https://example.com",
            "published": true
        }"#;

        doc_writer
            .add_document(doc_json)
            .expect("Failed to add document");
        doc_writer.commit().expect("Failed to commit");
    }

    #[test]
    #[serial(index)]
    fn test_add_document_with_numbers_and_bool() {
        let (_temp_dir, index_path) = get_temp_path("test_index_numbers");
        create_index(&index_path, &DOC_TEST_SCHEMA).expect("Failed to create index");

        let mut doc_writer =
            DocumentWriter::new(&index_path).expect("Failed to open index");

        let doc_json = r#"{
            "id": 2,
            "views": 123,
            "rating": 4.5,
            "published": false,
            "title": "Number and Bool Test",
            "content": "Testing numeric and boolean fields"
        }"#;

        doc_writer
            .add_document(doc_json)
            .expect("Failed to add document");
        doc_writer.commit().expect("Failed to commit");
    }

    #[test]
    #[serial(index)]
    fn test_add_document_with_array_field() {
        let (_temp_dir, index_path) = get_temp_path("test_index_array");
        create_index(&index_path, &DOC_TEST_SCHEMA).expect("Failed to create index");

        let mut doc_writer =
            DocumentWriter::new(&index_path).expect("Failed to open index");

        let doc_json = r#"{
            "id": 3,
            "title": "Array Field Test",
            "tags": ["rust", "tantivy", "serde"]
        }"#;

        doc_writer
            .add_document(doc_json)
            .expect("Failed to add document");
        doc_writer.commit().expect("Failed to commit");
    }

    #[test]
    #[serial(index)]
    fn test_add_document_partial_fields() {
        let (_temp_dir, index_path) = get_temp_path("test_index_partial");
        create_index(&index_path, &DOC_TEST_SCHEMA).expect("Failed to create index");

        let mut doc_writer =
            DocumentWriter::new(&index_path).expect("Failed to open index");

        // Missing optional fields like "content" and "tags"
        let doc_json = r#"{
            "id": 4,
            "title": "Partial Fields Test"
        }"#;

        doc_writer
            .add_document(doc_json)
            .expect("Failed to add document");
        doc_writer.commit().expect("Failed to commit");
    }

    #[test]
    #[serial(index)]
    fn test_add_document_invalid_json() {
        let (_temp_dir, index_path) = get_temp_path("test_index_invalid");
        create_index(&index_path, &DOC_TEST_SCHEMA).expect("Failed to create index");

        let mut doc_writer =
            DocumentWriter::new(&index_path).expect("Failed to open index");

        let doc_json = r#"{"id": 5, "title": "Invalid JSON""#; // missing closing }

        let result = doc_writer.add_document(doc_json);
        assert!(result.is_err(), "Expected JSON parse error");
    }

    #[test]
    #[serial(index)]
    fn test_commit_multiple_documents() {
        let (_temp_dir, index_path) = get_temp_path("test_index_multiple");
        create_index(&index_path, &DOC_TEST_SCHEMA).expect("Failed to create index");

        let mut doc_writer =
            DocumentWriter::new(&index_path).expect("Failed to open index");

        let docs = vec![
            r#"{"id": 6, "title": "Doc 1", "content": "First"}"#,
            r#"{"id": 7, "title": "Doc 2", "content": "Second"}"#,
            r#"{"id": 8, "title": "Doc 3", "content": "Third"}"#,
        ];

        for doc in docs {
            doc_writer
                .add_document(doc)
                .expect("Failed to add document");
        }

        doc_writer.commit().expect("Failed to commit");
    }
}
