package com.team2813;

import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

class Simple {
    public static void main(String[] args) throws Exception {
        String propertiesFile = "/git-info.properties";
        if (args.length >= 1) {
            propertiesFile = args[0];
        }
        Properties prop = new Properties();
        try (InputStream is = Simple.class.getResourceAsStream(propertiesFile)) {
            prop.load(is);
        }
        String sha = Objects.requireNonNull(prop.getProperty("git_sha"), "git_sha does not exist in properties!");
        if (sha.isBlank()) {
            throw new RuntimeException("git_sha is blank!");
        }
        String changes = Objects.requireNonNull(prop.getProperty("has_uncommited_changes"), "has_uncommited_changes does not exist in properties!");
        if (!changes.equals("true") && !changes.equals("false")) {
            throw new RuntimeException(String.format("Expected valid boolean for has_uncommited_changes, but was \"%s\"", changes));
        }
        boolean bothTrue = changes.equals("true") && Boolean.parseBoolean(changes);
        boolean bothFalse = changes.equals("false") && !Boolean.parseBoolean(changes);
        if (!bothTrue && !bothFalse) {
            throw new RuntimeException("\"True\" and \"False\" did not parse as proper booleans");
        }
        String commitDate = prop.getProperty("commit_date");
        if (commitDate != null) {
            Instant commitDateInst = Instant.parse(commitDate);
            if (commitDateInst.isAfter(Instant.now())) {
                throw new RuntimeException("Commit date is after the present!");
            }
        } else {
            throw new RuntimeException("Should have a valid commit date!");
        }
        String buildDate = prop.getProperty("build_date");
        if (buildDate != null) {
            Instant buildDateInst = Instant.parse(buildDate);
            if (buildDateInst.isAfter(Instant.now())) {
                throw new RuntimeException("Build date is after the present!");
            }
        } else {
            throw new RuntimeException("Should have a valid build date!");
        }
        String branchName = prop.getProperty("branch_name");
        if (branchName == null) {
            System.err.println("WARNING: no branch name!");
        }
    }
}