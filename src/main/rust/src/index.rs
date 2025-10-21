use std::path::Path;

use tantivy::schema::Schema;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum IndexError {
    #[error("Tantivy error: {0}")]
    Tantivy(#[from] tantivy::TantivyError),

    #[error("IO error: {0}")]
    Io(#[from] std::io::Error),

    #[error("Index already exists at path: {0}")]
    IndexAlreadyExists(String),
}

pub type IndexResult<T> = Result<T, IndexError>;

pub fn create_index<P: AsRef<Path>>(path: P, schema: &Schema) -> IndexResult<()> {
    let path = path.as_ref();
    if path.join("meta.json").exists() {
        return Err(IndexError::IndexAlreadyExists(
            path.to_string_lossy().to_string(),
        ));
    }
    std::fs::create_dir_all(path)?;
    tantivy::Index::create_in_dir(path, schema.clone())?;
    Ok(())
}

#[allow(dead_code)]
pub fn delete_index<P: AsRef<Path>>(path: P) -> IndexResult<()> {
    match std::fs::remove_dir_all(path) {
        Ok(()) => Ok(()),
        Err(e) if e.kind() == std::io::ErrorKind::NotFound => Ok(()),
        Err(e) => Err(IndexError::Io(e)),
    }
}

#[allow(dead_code)]
pub fn index_exists<P: AsRef<Path>>(path: P) -> bool {
    let path = path.as_ref();
    path.exists() && path.join("meta.json").exists()
}

pub fn open_index<P: AsRef<Path>>(path: P) -> IndexResult<tantivy::Index> {
    let path = path.as_ref();
    if !path.exists() {
        return Err(IndexError::Io(std::io::Error::new(
            std::io::ErrorKind::NotFound,
            format!("Index not found at path: {}", path.display()),
        )));
    }
    tantivy::Index::open_in_dir(path).map_err(IndexError::from)
}
