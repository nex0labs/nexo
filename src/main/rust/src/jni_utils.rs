use crate::index::IndexError;
use jni::JNIEnv;

pub fn validate_path(path: &str) -> Result<(), String> {
    if path.is_empty() {
        return Err("Path cannot be empty".to_string());
    }
    if path.contains("..") {
        return Err("Path cannot contain '..' for security reasons".to_string());
    }
    if path.len() > 1024 {
        return Err("Path is too long (max 1024 characters)".to_string());
    }
    Ok(())
}

pub fn convert_index_error(err: IndexError) -> tantivy::TantivyError {
    match err {
        IndexError::Tantivy(tantivy_err) => tantivy_err,
        IndexError::Io(io_err) => {
            tantivy::TantivyError::InvalidArgument(io_err.to_string())
        },
        IndexError::IndexAlreadyExists(path) => tantivy::TantivyError::InvalidArgument(
            format!("Index already exists at: {}", path),
        ),
    }
}

/// Throw a Java IOException
pub fn throw_java_exception(mut env: JNIEnv, msg: &str) {
    let _ = env.throw_new("java/io/IOException", msg);
}
