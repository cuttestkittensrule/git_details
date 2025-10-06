package com.team2813;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

class Simple {
    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (InputStream is = Simple.class.getResourceAsStream("/git-info.properties")) {
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
            System.err.println("WARNING: Ambiguous date!");
        }
        String branchName = prop.getProperty("branch_name");
        if (branchName == null) {
            System.err.println("WARNING: no branch name!");
        }
    }
}