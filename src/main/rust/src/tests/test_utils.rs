#![cfg(test)]
use lazy_static::lazy_static;
use std::path::PathBuf;
use tantivy::schema::{Schema, INDEXED, STORED, TEXT};
use tempfile::TempDir;
lazy_static! {
    pub static ref TEST_SCHEMA: Schema = {
        let mut schema_builder = Schema::builder();
        schema_builder.add_i64_field("id", INDEXED | STORED);
        schema_builder.add_text_field("title", TEXT | STORED);
        schema_builder.add_text_field("content", TEXT);
        schema_builder.add_text_field("url", TEXT);
        schema_builder.build()
    };
    pub static ref DOC_TEST_SCHEMA: Schema = {
        let mut schema_builder = Schema::builder();
        schema_builder.add_i64_field("id", INDEXED | STORED);
        schema_builder.add_i64_field("views", INDEXED | STORED);
        schema_builder.add_f64_field("rating", STORED);

        schema_builder.add_text_field("title", TEXT | STORED);
        schema_builder.add_text_field("content", TEXT);
        schema_builder.add_text_field("url", TEXT | STORED);

        schema_builder.add_bool_field("published", STORED);
        schema_builder.add_text_field("tags", TEXT | STORED);

        schema_builder.build()
    };
}

pub fn get_temp_path(index_name: &str) -> (TempDir, PathBuf) {
    let temp_dir = TempDir::new().expect("Failed to create temp dir");
    let index_path = temp_dir.path().join(index_name);
    (temp_dir, index_path)
}

pub fn json_schema() -> Schema {
    let schema_json = r#"[
        {
            "name": "title",
            "type": "text",
            "options": {
                "indexing": { "record": "position" },
                "stored": true
            }
        }
    ]"#;
    serde_json::from_str(schema_json).expect("Invalid schema JSON")
}
