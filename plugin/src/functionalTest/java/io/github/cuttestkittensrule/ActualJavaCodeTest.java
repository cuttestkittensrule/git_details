package io.github.cuttestkittensrule;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActualJavaCodeTest {
    @TempDir
    File projectDir;

    @Test
    void simpleWithDefaults() throws Exception {
        // Arrange
        Class<?> cls = ActualJavaCodeTest.class;
        FileLocations locations = new TestProjectBuilder(projectDir)
                .addSourceFile("com.team2813", cls.getResource("/Simple.java"))
                .mainClass("com.team2813.Simple")
                .build();

        // Act
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("run");
        runner.withProjectDir(projectDir);
        runner.build(); // assertions in Simple.java (resource); this will fail if Simple#main(String[]) fails

        // Assert
        assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"build.properties", "BUILD/git-info.properties", "Build/git.properties"})
    void simpleWithNonStandardLocation(String location) throws Exception {
        // Arrange
        Class<?> cls = ActualJavaCodeTest.class;
        FileLocations locations = new TestProjectBuilder(projectDir)
                .propertyPath(location)
                .addSourceFile("com.team2813", cls.getResource("/Simple.java"))
                .mainClass("com.team2813.Simple")
                .build();

        // Act
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("run", "--args", "/" + location);
        runner.withProjectDir(projectDir);
        runner.build(); // assertions in Simple.java (resource); this will fail if Simple#main(String[]) fails

        // Assert
        assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
    }

    @Test
    void gVersionCompatibility() throws Exception {
        // Arrange
        Class<?> cls = ActualJavaCodeTest.class;
        FileLocations locations = new TestProjectBuilder(projectDir)
                .gVersionCompatibility(true)
                .addSourceFile("com.team2813", cls.getResource("/GVersionCompatibility.java"))
                .mainClass("com.team2813.GVersionCompatibility")
                .build();

        // Act
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("run");
        runner.withProjectDir(projectDir);
        runner.build();

        // Assert
        assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"build.properties", "BUILD/git-info.properties", "Build/git.properties"})
    void gVersionCompatibilityWithNonStandardLocation(String location) throws Exception {
        // Arrange
        Class<?> cls = ActualJavaCodeTest.class;
        FileLocations locations = new TestProjectBuilder(projectDir)
                .gVersionCompatibility(true)
                .propertyPath(location)
                .addSourceFile("com.team2813", cls.getResource("/GVersionCompatibility.java"))
                .mainClass("com.team2813.GVersionCompatibility")
                .build();

        // Act
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("run", "--args", "/" + location);
        runner.withProjectDir(projectDir);
        runner.build();

        // Assert
        assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
    }
}
