package io.github.cuttestkittensrule;

import org.gradle.internal.impldep.org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TestProjectBuilder {
    private String propertyPath;
    private Boolean gVersionCompatibility;
    private String mainClass;
    private boolean createGitRepo = true;
    private final File projectDir;
    private final Map<String, List<URL>> srcFiles = new HashMap<>();

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
        this.propertyPath = Objects.requireNonNull(propertyPath, "Please pass a non-null property path");
        return this;
    }

    /**
     * Add a file to the source set
     * @param targetPackage The package to place the file in
     * @param file The URL of the file (from {@link Class#getResource(String) if it is a resource}
     * @return {@code this} for chaining
     */
    public TestProjectBuilder addSourceFile(String targetPackage, URL file) {
        List<URL> urls = srcFiles.computeIfAbsent(targetPackage, (unused) -> new ArrayList<>());
        urls.add(file);
        return this;
    }

    /**
     * Sets the main class of the project
     * @param mainClass The main class, as would be put into the {@code build.gradle} file
     * @return {@code this} for chaining
     */
    public TestProjectBuilder mainClass(String mainClass) {
        this.mainClass = Objects.requireNonNull(mainClass, "Please pass a non-null main class");
        return this;
    }

    /**
     * Sets if gversion compatibility should be enabled.
     * If this is not specified, the default configuration will be used.
     * @param gVersionCompatibility If gversion compatibility should be enabled
     * @return {@code this} for chaining
     */
    public TestProjectBuilder gVersionCompatibility(boolean gVersionCompatibility) {
        this.gVersionCompatibility = gVersionCompatibility;
        return this;
    }

    private static final String pluginStr = """
            plugins {
              id('java')
              id('io.github.cuttestkittensrule.git_details')
            }
            
            """;

    private FileLocations createFileLocations() {
        File buildFile = new File(projectDir, "build.gradle");
        File settingsFile = new File(projectDir, "settings.gradle");
        String relProperties = Optional.ofNullable(propertyPath).orElse("git-info.properties");
        List<String> relPath = new ArrayList<>(List.of("generated", "resources", "git_details"));
        relPath.addAll(List.of(relProperties.split("/")));
        File propertyFile = new File(projectDir, Path.of("build", relPath.toArray(String[]::new)).toString());
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
        if (propertyPath != null || gVersionCompatibility != null) {
            buildString.append("git_details {").append(System.lineSeparator());
            if (propertyPath != null) {
                buildString.append(String.format("  resourceFilePath = \"%s\"%n", propertyPath));
            }
            if (gVersionCompatibility != null) {
                buildString.append(String.format("  gversionBackwardCompatibility = %b%n", gVersionCompatibility));
            }
            buildString.append("}").append(System.lineSeparator());
        }
        if (mainClass != null) {
            String toAdd = String.format("""
                    apply plugin : "application"
                    application {
                      mainClassName = "%s"
                    }
                    
                    """, mainClass);
            buildString.append(toAdd);
        }
        writeString(locations.buildFile(), buildString.toString());
        if (!srcFiles.isEmpty()) {
            File baseSrc = new File(projectDir, Path.of("src", "main", "java").toString());
            if (!baseSrc.mkdirs()) {
                throw new RuntimeException("Could not create base directories!");
            }
            for (var entry : srcFiles.entrySet()) {
                List<String> components = new ArrayList<>(List.of(entry.getKey().split("\\.")));
                String first = components.remove(0);
                File dirToWrite = new File(baseSrc, Path.of(first, components.toArray(String[]::new)).toString());
                if (!dirToWrite.mkdirs()) {
                    throw new RuntimeException(String.format("Could not create folder for \"%s\"!", entry.getKey()));
                }
                for (URL url : entry.getValue()) {
                    Path sourcePath;
                    try {
                        sourcePath = Path.of(url.toURI());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Failed to convert url to uri!", e);
                    }
                    String filename = FilenameUtils.getName(url.getPath());
                    File targetFile = new File(dirToWrite, filename);
                    Files.copy(sourcePath, targetFile.toPath());
                }
            }

        }
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
