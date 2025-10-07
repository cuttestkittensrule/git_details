package com.team2813;

import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

class GVersionCompatibility {
    public static void main(String[] args) throws Exception {
        String propertiesFile = "/git-info.properties";
        if (args.length >= 1) {
            propertiesFile = args[0];
        }
        Properties prop = new Properties();
        try (InputStream is = GVersionCompatibility.class.getResourceAsStream(propertiesFile)) {
            prop.load(is);
        }
        String sha = Objects.requireNonNull(prop.getProperty("git_sha"), "git_sha does not exist in properties!");
        if (sha.isBlank()) {
            throw new RuntimeException("git_sha is blank!");
        }
        String dirty = Objects.requireNonNull(prop.getProperty("dirty"), "dirty does not exist in properties!");
        int dirtyInt = Integer.parseInt(dirty);
        if (dirtyInt != 0 && dirtyInt != 1) {
            throw new RuntimeException(String.format("dirty does not have a valid value! Expected 0 or 1, but was %s!", dirtyInt));
        }
        String commitDate = prop.getProperty("git_date");
        if (commitDate != null) {
            Instant commitDateInst = Instant.parse(commitDate);
            if (commitDateInst.isAfter(Instant.now())) {
                throw new RuntimeException("Commit date is after the present!");
            }
        } else {
            throw new RuntimeException("Git should generate a valid date!");
        }
        String branchName = prop.getProperty("branch_name");
        if (branchName == null) {
            System.err.println("WARNING: no branch name!");
        }
    }
}