#[cfg(feature = "jni")]
mod jni;

use std::fs;
use std::io::Write;
use std::{fs::File, path::Path};

use bitflags::bitflags;
use chrono::{DateTime, FixedOffset, Local, TimeZone};
use git2::{Branch, Repository, StatusOptions, Time};

pub type Error = Box<dyn std::error::Error>;
pub type Result<T> = std::result::Result<T, Error>;

bitflags! {
    #[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
    pub struct Options: u32 {
        const GVERSION_COMPAT = 1;
        const GEN_BUILD_DATE = 1 << 1;
        const USE_LATEST_DATE = 1 << 2;
    }
}

#[derive(Debug)]
pub struct Results {
    options: Options,
    sha: String,
    commit_time: Option<DateTime<FixedOffset>>,
    dirty: bool,
    branch_name: Option<String>,
    build_time: Option<DateTime<Local>>,
}

impl Results {
    fn calculate_time(git_time: Time, options: Options) -> Option<DateTime<FixedOffset>> {
        let offset = FixedOffset::east_opt(git_time.offset_minutes() * 60)?;
        let local_result = offset.timestamp_millis_opt(git_time.seconds() * 1_000);
        if options.contains(Options::USE_LATEST_DATE) {
            local_result.latest()
        } else {
            local_result.earliest()
        }
    }
    pub fn options_new<P: AsRef<Path>>(filepath: P, options: Options) -> Option<Results> {
        let build_time = if options.contains(Options::GVERSION_COMPAT) {
            Some(Local::now())
        } else {
            None
        };
        
        let repo = Repository::open(filepath).ok()?;
        let head = repo.head().ok()?;
        let commit = head.peel_to_commit().ok()?;
        let sha = format!("{:?}", commit.id());
        let commit_time = Self::calculate_time(commit.time(), options);
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
            options,
            sha,
            commit_time,
            dirty,
            branch_name,
            build_time,
        })
    }
    pub fn new<P: AsRef<Path>>(filepath: P) -> Option<Results> {
        Self::options_new(filepath, Options::empty())
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
            if self.options.contains(Options::GVERSION_COMPAT) {
                writeln!(file, "git_date={commit_time:?}")?;
            } else {
                writeln!(file, "commit_date={commit_time:?}")?;
            }
        }
        if let Some(build_date) = self.build_time {
            writeln!(file, "build_date={build_date:?}")?;
        }
        writeln!(file, "{}", COMPAT_MSG)?;
        if self.options.contains(Options::GVERSION_COMPAT) {
            writeln!(file, "dirty={}", self.dirty as u8)?;
        } else {
            writeln!(file, "has_uncommited_changes={}", self.dirty)?;
        }
        if let Some(branch_name) = self.branch_name {
            writeln!(file, "branch_name={branch_name}")?;
        }
        Ok(())
    }
}
