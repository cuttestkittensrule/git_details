use std::env;
use std::fs;
use std::io::Write;
use std::{fs::File, path::Path};

use chrono::{DateTime, FixedOffset, TimeZone};
use git2::{Branch, Repository, StatusOptions, Time};

pub type Error = Box<dyn std::error::Error>;
pub type Result<T> = std::result::Result<T, Error>;

#[derive(Debug)]
pub struct Results {
    sha: String,
    commit_time: Option<DateTime<FixedOffset>>,
    dirty: bool,
    branch_name: Option<String>,
}

impl Results {
    fn calculate_time(git_time: Time) -> Option<DateTime<FixedOffset>> {
        let offset = FixedOffset::east_opt(git_time.offset_minutes() * 60)?;
        offset
            .timestamp_millis_opt(git_time.seconds() * 1_000)
            .single()
    }
    pub fn new<P: AsRef<Path>>(filepath: P) -> Option<Results> {
        let repo = Repository::open(filepath).ok()?;
        let head = repo.head().ok()?;
        let commit = head.peel_to_commit().ok()?;
        let sha = format!("{:?}", commit.id());
        let commit_time = Self::calculate_time(commit.time());
        let dirty = !repo
            .statuses(Some(StatusOptions::new().include_ignored(false)))
            .ok()?
            .is_empty();
        let branch_name = if head.is_branch() {
            let head = Branch::wrap(head);
            head.name().ok().flatten().map(|x| x.to_string())
        } else {
            None
        };

        Some(Results {
            sha,
            commit_time,
            dirty,
            branch_name,
        })
    }
    pub fn sha(&self) -> &str {
        &self.sha
    }
    pub fn commit_time(&self) -> Option<DateTime<FixedOffset>> {
        self.commit_time
    }
    pub fn dirty(&self) -> bool {
        self.dirty
    }
    pub fn branch_name(&self) -> Option<&str> {
        match self.branch_name {
            Some(ref string) => Some(string.as_str()),
            None => None,
        }
    }
    pub fn create_java_properties<P: AsRef<Path>>(self, filepath: P) -> Result<()> {
        const COMPAT_MSG: &str = "# The following two values are identical; prior is deprecated and kept for gversion compatability";
        fs::create_dir_all(filepath.as_ref().parent().ok_or_else(|| Error::from("Cannot get parent directory!"))?)?;
        let mut file = File::create(filepath)?;
        writeln!(file, "git_sha={}", self.sha)?;
        if let Some(commit_time) = self.commit_time {
            writeln!(file, "{}", COMPAT_MSG)?;
            writeln!(file, "git_date={commit_time:?}")?;
            writeln!(file, "commit_date={commit_time:?}")?;
        }
        writeln!(file, "{}", COMPAT_MSG)?;
        writeln!(file, "dirty={}", self.dirty as u8)?;
        writeln!(file, "has_uncommited_changes={}", self.dirty)?;
        if let Some(branch_name) = self.branch_name {
            writeln!(file, "branch_name={branch_name}")?;
        }
        Ok(())
    }
}

fn main() -> Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() != 3 {
        eprintln!("Usage: {} <git_repo_path> <output_file_path>", args[0]);
        return Err("Invalid number of arguments provided.".into());
    }
    let repo_path = &args[1];
    let output_file_path = &args[2];

    let results = Results::new(repo_path)
        .ok_or_else(|| {
            let msg = format!("Could not initialize Git details. Path '{}' is not a valid Git repository.", repo_path);
            Box::from(msg) as Box<dyn std::error::Error>
        })?;
    results.create_java_properties(output_file_path)?;
    Ok(())
}
