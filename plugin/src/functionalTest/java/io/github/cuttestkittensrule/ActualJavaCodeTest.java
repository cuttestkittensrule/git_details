/*
Copyright 2025 Kyle C

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package io.github.cuttestkittensrule;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ActualJavaCodeTest {
  @TempDir File projectDir;

  @Test
  void simpleWithDefaults() throws Exception {
    // Arrange
    Class<?> cls = ActualJavaCodeTest.class;
    FileLocations locations =
        new TestProjectBuilder(projectDir)
            .addSourceFile("io.github.cuttestkittensrule", cls.getResource("/Simple.java"))
            .mainClass("io.github.cuttestkittensrule.Simple")
            .build();

    // Act
    GradleRunner runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments("run");
    runner.withProjectDir(projectDir);
    // assertions in Simple.java (resource); this will fail if Simple#main(String[]) fails
    runner.build();

    // Assert
    assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
  }

  @ParameterizedTest
  @ValueSource(strings = {"build.properties", "BUILD/git-info.properties", "Build/git.properties"})
  void simpleWithNonStandardLocation(String location) throws Exception {
    // Arrange
    Class<?> cls = ActualJavaCodeTest.class;
    FileLocations locations =
        new TestProjectBuilder(projectDir)
            .propertyPath(location)
            .addSourceFile("io.github.cuttestkittensrule", cls.getResource("/Simple.java"))
            .mainClass("io.github.cuttestkittensrule.Simple")
            .build();

    // Act
    GradleRunner runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments("run", "--args", "/" + location);
    runner.withProjectDir(projectDir);
    // assertions in Simple.java (resource); this will fail if Simple#main(String[]) fails
    runner.build();

    // Assert
    assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
  }

  @Test
  void gVersionCompatibility() throws Exception {
    // Arrange
    Class<?> cls = ActualJavaCodeTest.class;
    FileLocations locations =
        new TestProjectBuilder(projectDir)
            .gVersionCompatibility(true)
            .addSourceFile(
                "io.github.cuttestkittensrule", cls.getResource("/GVersionCompatibility.java"))
            .mainClass("io.github.cuttestkittensrule.GVersionCompatibility")
            .build();

    // Act
    GradleRunner runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments("run");
    runner.withProjectDir(projectDir);
    // assertions in GVersionCompatibility.java (resource); this will fail if
    // GVersionCompatibility#main(String[]) fails
    runner.build();

    // Assert
    assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
  }

  @ParameterizedTest
  @ValueSource(strings = {"build.properties", "BUILD/git-info.properties", "Build/git.properties"})
  void gVersionCompatibilityWithNonStandardLocation(String location) throws Exception {
    // Arrange
    Class<?> cls = ActualJavaCodeTest.class;
    FileLocations locations =
        new TestProjectBuilder(projectDir)
            .gVersionCompatibility(true)
            .propertyPath(location)
            .addSourceFile(
                "io.github.cuttestkittensrule", cls.getResource("/GVersionCompatibility.java"))
            .mainClass("io.github.cuttestkittensrule.GVersionCompatibility")
            .build();

    // Act
    GradleRunner runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments("run", "--args", "/" + location);
    runner.withProjectDir(projectDir);
    // assertions in GVersionCompatibility.java (resource); this will fail if
    // GVersionCompatibility#main(String[]) fails
    runner.build();

    // Assert
    assertTrue(locations.expectedPropertyFile().exists(), "Expected property file doesn't exist!");
  }
}
