package com.team2813.gradle;

import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TestProjectBuilder {
    private String propertyPath;
    private boolean createGitRepo = true;
    private final File projectDir;

    public TestProjectBuilder(File projectDir) {
        this.projectDir = projectDir;
    }

    /**
     * Don't create a git repository for the project.
     * @return {@code this} for chaining
     */
    public TestProjectBuilder withoutGitRepo() {
        createGitRepo = false;
        return this;
    }

    /**
     * Specify the configured property path.
     * @param propertyPath The path to configure the plugin to use
     * @return {@code this} for chaining
     * @throws NullPointerException if {@code propertyPath} is {@code null}
     */
    public TestProjectBuilder propertyPath(String propertyPath) {
        propertyPath = Objects.requireNonNull(propertyPath, "Please pass a non-null property path");
        return this;
    }

    private static final String pluginStr = """
            plugins {
              id('java')
              id('com.team2813.gradle.git_details')
            }
            
            """;

    private FileLocations createFileLocations() {
        File buildFile = new File(projectDir, "build.gradle");
        File settingsFile = new File(projectDir, "settings.gradle");
        String relProperties = Optional.ofNullable(propertyPath).orElse("git-info.properties");
        List<String> relPath = new ArrayList<>(List.of("resources", "git_version"));
        relPath.addAll(List.of(relProperties.split("/")));
        File propertyFile = new File(projectDir, Path.of("generated", relPath.toArray(String[]::new)).toString());
        return new FileLocations(
                buildFile,
                settingsFile,
                propertyFile
        );
    }

    public FileLocations build() throws IOException, InterruptedException {
        FileLocations locations = createFileLocations();
        writeString(locations.settingsFile(), "");
        StringBuilder buildString = new StringBuilder(pluginStr);
        if (propertyPath != null) {
            String toAdd = String.format("""
                    git_version {
                      resourceFilePath = "%s"
                    }
                    """, propertyPath);
            buildString.append(toAdd);
        }
        writeString(locations.buildFile(), buildString.toString());
        if (createGitRepo) {
            try {
                // init git repository in the temporary directory
                new ProcessBuilder()
                        .directory(projectDir)
                        .command("git", "init")
                        .start().waitFor();
                // add the build.gradle and the settings.gradle files
                new ProcessBuilder()
                        .directory(projectDir)
                        .command("git", "add", "-A")
                        .start().waitFor();
                // commit those files (without signatures since we don't need them)
                new ProcessBuilder()
                        .directory(projectDir)
                        .command("git", "commit", "--no-gpg-sign", "-m", "initial commit")
                        .start().waitFor();
            } catch (IOException e) {
                // if we had an IOException, we couldn't find git, or it fails. Either way, abort the test
                Assumptions.abort("Could not execute git commands, so the temporary folder won't be a valid git repository, which would wrongly fail the test!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        return locations;
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }

}
