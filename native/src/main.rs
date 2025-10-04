use std::{env, path::PathBuf};

use git_details::{Result, Results, Error};

fn main() -> Result<()> {
    let filepath = env::args()
        .nth(1)
        .map(PathBuf::from)
        .filter(|path| {
            let exists = path.exists();
            if !exists {
                eprintln!("WARNING: \"{path:?}\" does not exist! Using current directory")
            }
            exists
        })
        .ok_or_else(|| {
            Box::from("No argument was supplied, or it didn't exist and couldn't get the current directory!")
        })
        .or_else(|_: Error| env::current_dir())?;

    let results = Results::new(filepath);
    println!("full: {:?}", results);

    if let Some((filepath, results)) = env::args().nth(2).map(PathBuf::from).zip(results) {
        eprintln!("Creating java properties file with path {filepath:?}...");
        if let Err(err) = results.create_java_properties(filepath) {
            eprintln!("Error occured while creating java properties file!");
            return Err(err);
        }
    }

    Ok(())
}
