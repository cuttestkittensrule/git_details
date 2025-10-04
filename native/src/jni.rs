use std::num::NonZero;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jint;

use crate::Results;

type Result<T> = std::result::Result<T, NonZero<jint>>;

macro_rules! err_code {
    (0) => { compile_error!("Error code cannot be 0!") };
    (0b0000_0000) => { compile_error!("Error code cannot be 0!") };
    ($code:literal) => {
        {
            let code: jint = $code;
            assert_ne!(code, 0);
            unsafe { NonZero::new_unchecked(code) }
        }
    };
}

/// Generates a Java properties file with git information.
///
/// # Arguments
///
/// [`repo_path`]: The path to the git repository
/// [`property_path`]: The path to the properties file
///
/// # Error Codes
///
/// 0b0001_0001 (0x11): Failed to get repo path
/// 0b0001_0010 (0x12): Failed to get property path
/// 0b0010_0001 (0x21): Fundamental assumption of state of git failed
/// 0b0010_0010 (0x22): IO error while creating properties file (details in stderr)
///
/// # Fundamental assumptions
///
/// * The git repository can be opened
/// * The head reference exists
/// * The head reference can be peeled to a commit
///   * When recursively following the head reference, eventually it will point to a commit
/// * The current status of the repo can be queried
///
/// All other error cases are represented by properties not being generated in the properties file.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_team2813_gradle_GitVersionPlugin_generateGitProperties<'local>(
    env: JNIEnv<'local>,
    _: JClass<'local>,
    repo_path: JString<'local>,
    property_path: JString<'local>,
) -> jint {
    match inner_gen_git_prop(env, repo_path, property_path) {
        Err(ecode) => ecode.get(),
        Ok(_) => 0,
    }
}

fn inner_gen_git_prop<'local>(
    mut env: JNIEnv<'local>,
    repo_path: JString<'local>,
    property_path: JString<'local>,
) -> Result<()> {
    let repo_path: String = env.get_string(&repo_path).map_err(|_| err_code!(0b0001_0001))?.into();
    let property_path: String = env
        .get_string(&property_path)
        .map_err(|_| err_code!(0b0001_0010))?
        .into();
    let results = Results::new(repo_path).ok_or(err_code!(0b0010_0001))?;
    results
        .create_java_properties(property_path)
        .map_err(|err| {
            eprintln!("An error occured while writing the properties file: {err:?}");
            err_code!(0b0010_0010)
        })
}
