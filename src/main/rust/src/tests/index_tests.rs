#[cfg(test)]
mod tests {

    use crate::tests::test_utils::{get_temp_path, json_schema, TEST_SCHEMA};
    use crate::{create_index, delete_index, open_index};
    use serial_test::serial;

    #[test]
    #[serial(index)]
    fn test_create_index() {
        let (temp_dir, index_path) = get_temp_path("test_index");
        create_index(&index_path, &TEST_SCHEMA).expect("Failed to create index");
        assert!(index_path.exists(), "Index directory was not created");
        assert!(
            index_path.join("meta.json").exists(),
            "meta.json was not created"
        );

        temp_dir.close().expect("Failed to close temp dir");
    }

    #[test]
    #[serial(index)]
    fn test_create_index_with_json_schema() {
        let (temp_dir, index_path) = get_temp_path("test_index_json");
        create_index(&index_path, &json_schema())
            .expect("Failed to create index from JSON schema");
        assert!(index_path.exists(), "Index directory was not created");
        assert!(
            index_path.join("meta.json").exists(),
            "meta.json was not created"
        );
        temp_dir.close().expect("Failed to close temp dir");
    }

    #[test]
    #[serial(index)]
    fn test_create_index_already_exists() {
        let (temp_dir, index_path) = get_temp_path("test_index");
        create_index(&index_path, &TEST_SCHEMA).expect("Failed to create index");
        let result = create_index(&index_path, &TEST_SCHEMA);
        assert!(
            result.is_err(),
            "Should fail to create index that already exists"
        );

        temp_dir.close().expect("Failed to close temp dir");
    }

    #[test]
    #[serial(index)]
    fn test_open_index() {
        let (temp_dir, index_path) = get_temp_path("test_index");
        create_index(&index_path, &TEST_SCHEMA).expect("Failed to create index");
        let _opened_index = open_index(&index_path).expect("Failed to open index");
        temp_dir.close().expect("Failed to close temp dir");
    }

    #[test]
    #[serial(index)]
    fn test_open_nonexistent_index() {
        let (temp_dir, index_path) = get_temp_path("nonexistent_index");
        let result = open_index(&index_path);
        assert!(result.is_err(), "Should fail to open non-existent index");
        temp_dir.close().expect("Failed to close temp dir");
    }

    #[test]
    #[serial(index)]
    fn test_delete_index() {
        let (temp_dir, index_path) = get_temp_path("test-index");
        create_index(&index_path, &TEST_SCHEMA).expect("Failed to create index");
        delete_index(&index_path).expect("Failed to delete index");
        assert!(
            !index_path.exists(),
            "Index directory still exists after deletion"
        );
        temp_dir.close().expect("Failed to close temp dir");
    }

    #[test]
    #[serial(index)]
    fn test_delete_nonexistent_index() {
        let (temp_dir, index_path) = get_temp_path("nonexistent_index");
        delete_index(&index_path).expect("Failed to delete non-existent index");
        temp_dir.close().expect("Failed to close temp dir");
    }
}
