use serde_json::Value;
use std::path::Path;
use tantivy::schema::{Field, Schema};
use tantivy::{Index, IndexWriter, TantivyDocument};

use thiserror::Error;

#[warn(dead_code)]
#[derive(Error, Debug)]
pub enum DocumentError {
    #[error("Tantivy error: {0}")]
    Tantivy(#[from] tantivy::TantivyError),
    #[error("IO error: {0}")]
    Io(#[from] std::io::Error),
    #[error("Schema error: {0}")]
    JsonParse(String),
    #[error("Field not found: {0}")]
    FieldNotFound(String),
    #[error("Error parsing field: {0}")]
    FieldError(String),
}

pub type DocumentResult<T> = Result<T, DocumentError>;
const MAX_MEMORY_LIMIT: usize = 50_000_000;

pub struct DocumentWriter {
    index_writer: IndexWriter,
    schema: Schema,
}

impl DocumentWriter {
    pub fn new<P: AsRef<Path>>(index_path: P) -> DocumentResult<Self> {
        let index = Index::open_in_dir(index_path)?;
        let writer = index.writer(MAX_MEMORY_LIMIT)?;
        Ok(Self {
            index_writer: writer,
            schema: index.schema(),
        })
    }

    pub fn add_document(&mut self, doc_json: &str) -> DocumentResult<()> {
        let doc = parse_document(&self.schema, doc_json)?;
        self.index_writer.add_document(doc)?;
        Ok(())
    }

    pub fn commit(&mut self) -> DocumentResult<()> {
        self.index_writer.commit()?;
        Ok(())
    }
}
fn add_number(
    doc: &mut TantivyDocument,
    field: Field,
    n: &serde_json::Number,
) -> DocumentResult<()> {
    if let Some(i) = n.as_i64() {
        doc.add_i64(field, i);
    } else if let Some(f) = n.as_f64() {
        doc.add_f64(field, f);
    } else {
        return Err(DocumentError::FieldError("Invalid number".into()));
    }
    Ok(())
}

fn add_value(
    doc: &mut TantivyDocument,
    field: Field,
    value: &Value,
) -> DocumentResult<()> {
    match value {
        Value::String(s) => {
            doc.add_text(field, s);
            Ok(())
        },
        Value::Number(n) => add_number(doc, field, n),
        Value::Bool(b) => {
            doc.add_bool(field, *b);
            Ok(())
        },
        _ => Err(DocumentError::FieldError(
            "Unsupported field value type".into(),
        )),
    }
}
fn parse_document(schema: &Schema, json_doc: &str) -> DocumentResult<TantivyDocument> {
    let doc_value: Value = serde_json::from_str(json_doc)
        .map_err(|e| DocumentError::JsonParse(e.to_string()))?;
    let mut doc = TantivyDocument::default();
    let obj = doc_value
        .as_object()
        .ok_or_else(|| DocumentError::JsonParse("Expected JSON object".to_string()))?;
    for (field_name, field_value) in obj {
        let field = schema
            .get_field(field_name)
            .map_err(|_| DocumentError::FieldNotFound(field_name.clone()))?;
        match field_value {
            Value::Array(arr) => arr
                .iter()
                .try_for_each(|item| add_value(&mut doc, field, item))?,
            other => {
                add_value(&mut doc, field, other)?;
            },
        }
    }

    Ok(doc)
}
