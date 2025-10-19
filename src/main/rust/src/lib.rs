// Core modules
mod doc_writer;
mod index;
mod tests;

// JNI bindings
mod jni_fs_index;
mod jni_utils;

use jni::objects::{JClass, JString};
use jni::sys::{jint, jlong};
use jni::JNIEnv;

use crate::doc_writer::DocumentWriter;
use crate::jni_utils::{throw_java_exception, validate_path};

//Document Processing
#[no_mangle]
pub extern "system" fn Java_com_nexo_core_index_TantivyIndex_writerNative(
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
pub extern "system" fn Java_com_nexo_core_index_TantivyIndex_addDocumentNative(
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
pub extern "system" fn Java_com_nexo_core_index_TantivyIndex_commitWriterNative(
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
pub extern "system" fn Java_com_nexo_core_index_TantivyIndex_closeWriterNative(
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
