mod doc_writer;
mod index;
mod tests;
use std::path::Path;

use jni::objects::{JClass, JString};
use jni::sys::jlong;
use jni::sys::{jboolean, jint};

use crate::doc_writer::DocumentWriter;
use crate::index::{create_index, delete_index, index_exists, open_index, IndexError};
use jni::JNIEnv;
use tantivy::schema::Schema;

fn convert_index_error(err: IndexError) -> tantivy::TantivyError {
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
#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_tantivy_FSDirectory_createIndexNative(
    mut env: JNIEnv,
    _class: JClass,
    index_path: JString,
    schema_json: JString,
) -> jboolean {
    let result: tantivy::Result<()> = (|| {
        let index_path: String = env
            .get_string(&index_path)
            .map_err(|e| tantivy::TantivyError::InvalidArgument(e.to_string()))?
            .into();
        let schema_json: String = env
            .get_string(&schema_json)
            .map_err(|e| tantivy::TantivyError::InvalidArgument(e.to_string()))?
            .into();

        validate_path(&index_path).map_err(tantivy::TantivyError::InvalidArgument)?;

        if schema_json.len() > 1024 * 1024 {
            return Err(tantivy::TantivyError::InvalidArgument(
                "Schema JSON is too large (max 1MB)".to_string(),
            ));
        }

        let schema: Schema = serde_json::from_str(&schema_json).map_err(|e| {
            tantivy::TantivyError::InvalidArgument(format!("Invalid schema JSON: {}", e))
        })?;

        create_index(Path::new(&index_path), &schema).map_err(convert_index_error)?;
        Ok(())
    })();

    match result {
        Ok(()) => jni::sys::JNI_TRUE,
        Err(err) => {
            throw_java_exception(
                env,
                &format!("Index Create Runtime Exception {:?}", err),
            );
            jni::sys::JNI_FALSE
        },
    }
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_tantivy_FSDirectory_deleteIndexNative(
    mut env: JNIEnv,
    _class: JClass,
    index_path: JString,
) -> jboolean {
    let result: tantivy::Result<()> = (|| {
        let index_path: String = env
            .get_string(&index_path)
            .map_err(|e| tantivy::TantivyError::InvalidArgument(e.to_string()))?
            .into();

        validate_path(&index_path).map_err(tantivy::TantivyError::InvalidArgument)?;

        delete_index(Path::new(&index_path)).map_err(convert_index_error)?;
        Ok(())
    })();

    match result {
        Ok(()) => jni::sys::JNI_TRUE,
        Err(err) => {
            throw_java_exception(
                env,
                &format!("Index Delete Runtime Exception {:?}", err),
            );
            jni::sys::JNI_FALSE
        },
    }
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_tantivy_FSDirectory_indexExistsNative(
    mut env: JNIEnv,
    _class: JClass,
    index_path: JString,
) -> jboolean {
    let index_path: String = match env.get_string(&index_path) {
        Ok(s) => s.into(),
        Err(e) => {
            throw_java_exception(env, &format!("Invalid index path argument: {}", e));
            return jni::sys::JNI_FALSE;
        },
    };

    if let Err(e) = validate_path(&index_path) {
        throw_java_exception(env, &e);
        return jni::sys::JNI_FALSE;
    }

    if index_exists(Path::new(&index_path)) {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_tantivy_FSDirectory_openIndexNative(
    mut env: JNIEnv,
    _class: JClass,
    index_path: JString,
) -> jboolean {
    let result: tantivy::Result<()> = (|| {
        let index_path: String = env
            .get_string(&index_path)
            .map_err(|e| tantivy::TantivyError::InvalidArgument(e.to_string()))?
            .into();

        let _index = open_index(Path::new(&index_path)).map_err(convert_index_error)?;
        Ok(())
    })();

    match result {
        Ok(()) => jni::sys::JNI_TRUE,
        Err(err) => {
            throw_java_exception(env, &format!("Index open Failed{:?}", err));
            jni::sys::JNI_FALSE
        },
    }
}

//Document Processing
#[no_mangle]
pub extern "system" fn Java_com_tantivy_IndexWriter_writerNative(
    mut env: JNIEnv,
    _class: JClass,
    index_path: JString,
) -> jlong {
    let path: String = match env.get_string(&index_path) {
        Ok(s) => s.into(),
        Err(e) => {
            throw_java_exception(env, &format!("Invalid path string: {:?}", e));
            return 0;
        },
    };

    if let Err(e) = validate_path(&path) {
        throw_java_exception(env, &e);
        return 0;
    }

    match DocumentWriter::new(path) {
        Ok(writer) => Box::into_raw(Box::new(writer)) as jlong,
        Err(e) => {
            throw_java_exception(env, &format!("Failed to create writer: {}", e));
            0
        },
    }
}

#[no_mangle]
pub extern "system" fn Java_com_tantivy_IndexWriter_addDocumentNative(
    mut env: JNIEnv,
    _class: jni::objects::JClass,
    handle: jlong,
    documents: JString,
) {
    if handle == 0 {
        throw_java_exception(env, "Invalid writer handle (null)");
        return;
    }

    let writer = unsafe { &mut *(handle as *mut DocumentWriter) };

    let doc: String = match env.get_string(&documents) {
        Ok(s) => s.into(),
        Err(e) => {
            throw_java_exception(env, &format!("Invalid document string: {:?}", e));
            return;
        },
    };

    if doc.len() > 10 * 1024 * 1024 {
        throw_java_exception(env, "Document is too large (max 10MB)");
        return;
    }

    if let Err(e) = writer.add_document(&doc) {
        throw_java_exception(env, &format!("Failed to add document: {}", e));
    }
}

#[no_mangle]
pub extern "system" fn Java_com_tantivy_IndexWriter_commitWriterNative(
    env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jint {
    if handle == 0 {
        throw_java_exception(env, "Invalid writer handle (null)");
        return -1;
    }
    let writer = unsafe { &mut *(handle as *mut DocumentWriter) };
    match writer.commit() {
        Ok(_) => 0,
        Err(e) => {
            throw_java_exception(env, &format!("Commit failed: {}", e));
            -1
        },
    }
}
#[no_mangle]
pub extern "system" fn Java_com_tantivy_IndexWriter_closeWriterNative(
    _env: JNIEnv,
    _class: jni::objects::JClass,
    handle: jlong,
) {
    if handle != 0 {
        unsafe {
            let _ = Box::from_raw(handle as *mut DocumentWriter);
        }
    }
}

fn validate_path(path: &str) -> Result<(), String> {
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

fn throw_java_exception(mut env: JNIEnv, msg: &str) {
    let _ = env.throw_new("java/io/IOException", msg);
}
