use jni::objects::{JClass, JString};
use jni::sys::jboolean;
use jni::JNIEnv;
use std::path::Path;
use tantivy::schema::Schema;

use crate::index::{create_index, open_index};
use crate::jni_utils::{convert_index_error, throw_java_exception, validate_path};

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_nexo_index_TantivyIndex_createIndexNative(
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

        validate_path(&index_path).map_err(tantivy::TantivyError::InvalidArgument)?;

        let schema_json: String = env
            .get_string(&schema_json)
            .map_err(|e| tantivy::TantivyError::InvalidArgument(e.to_string()))?
            .into();

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
            throw_java_exception(env, &format!("Failed to create FS index: {:?}", err));
            jni::sys::JNI_FALSE
        },
    }
}

#[no_mangle]
pub extern "system" fn Java_com_nexo_index_TantivyIndex__openIndexNative(
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

        let _index = open_index(Path::new(&index_path)).map_err(convert_index_error)?;

        Ok(())
    })();

    match result {
        Ok(()) => jni::sys::JNI_TRUE,
        Err(err) => {
            throw_java_exception(env, &format!("Failed to open FS index: {:?}", err));
            jni::sys::JNI_FALSE
        },
    }
}
