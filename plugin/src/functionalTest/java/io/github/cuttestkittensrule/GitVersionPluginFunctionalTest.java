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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** A simple functional test for the 'com.team2813.git_details.greeting' plugin. */
class GitVersionPluginFunctionalTest {
  @TempDir File projectDir;

  @Test
  void defaultPropertyFileCreated() throws Exception {
    // Prepare
    FileLocations locations = new TestProjectBuilder(projectDir).build();

    // Act (run createGitProperties)
    GradleRunner runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments("createGitProperties");
    runner.withProjectDir(projectDir);
    runner.build();

    // Assert
    assertTrue(locations.expectedPropertyFile().exists(), "Properties file does not exist!");
  }

  @ParameterizedTest
  @ValueSource(strings = {"build.properties", "BUILD/git-info.properties", "Build/git.properties"})
  void nonDefaultPropertyFileCreated(String propertyLocation) throws Exception {
    // Prepare
    FileLocations locations =
        new TestProjectBuilder(projectDir).propertyPath(propertyLocation).build();

    // Act (run createGitProperties)
    GradleRunner runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments("createGitProperties");
    runner.withProjectDir(projectDir);
    runner.build();

    // Assert
    assertTrue(locations.expectedPropertyFile().exists(), "Properties file does not exist!");
  }

  @Test
  void noGitRepository() throws Exception {
    // Prepare
    FileLocations locations = new TestProjectBuilder(projectDir).withoutGitRepo().build();

    // Act (run createGitProperties)
    GradleRunner runner = GradleRunner.create();
    runner.withPluginClasspath();
    runner.withArguments("createGitProperties");
    runner.withProjectDir(projectDir);
    BuildResult result = runner.buildAndFail();

    // Assert
    assertFalse(
        locations.expectedPropertyFile().exists(),
        "Properties file should not be created on failure!");
    assertTrue(
        result.getOutput().contains("A fundamental assumption of git state was broken!"),
        "Should have the exception message in output!");
  }
}
