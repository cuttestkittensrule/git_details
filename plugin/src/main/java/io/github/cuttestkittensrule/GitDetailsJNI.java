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

import fr.stardustenterprises.plat4k.EnumArchitecture;
import fr.stardustenterprises.plat4k.EnumOperatingSystem;
import fr.stardustenterprises.plat4k.Platform;
import fr.stardustenterprises.yanl.NativeLoader;
import fr.stardustenterprises.yanl.PlatformContext;
import fr.stardustenterprises.yanl.api.Context;

class GitDetailsJNI {
  private static class CorrectContext implements Context {
    private final boolean applyHack;
    private final PlatformContext platformContext = new PlatformContext();

    CorrectContext() {
      Platform platform = Platform.getCurrentPlatform();
      applyHack =
          platform.getArchitecture().equals(EnumArchitecture.AARCH_64)
              && platform.getOperatingSystem().equals(EnumOperatingSystem.WINDOWS);
    }

    @Override
    public String getArchIdentifier() {
      return platformContext.getArchIdentifier();
    }

    @Override
    public String getOsName() {
      if (applyHack) {
        // default is broken; puts aarch64 windows in windows-llvm
        // force os name to windows-llvm to make the path correct on aarch64 windows
        return "windows-llvm";
      }
      return platformContext.getOsName();
    }

    @Override
    public boolean is64Bits() {
      return platformContext.is64Bits();
    }

    @Override
    public String mapLibraryName(
        String name, boolean usePlatformPrefix, boolean usePlatformSuffix) {
      return platformContext.mapLibraryName(name, usePlatformPrefix, usePlatformSuffix);
    }

    @Override
    public String format(String pathFormat, String name) {
      return platformContext.format(pathFormat, name);
    }
  }

  private static final String BINARY_NAME = "git_details";

  static {
    NativeLoader loader = new NativeLoader.Builder().context(new CorrectContext()).build();
    try {
      loader.loadLibrary(BINARY_NAME, false);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new RuntimeException("Failed to load library", e);
    }
  }

  /**
   * Generate a Java properties file with git information.
   *
   * @param repoPath The path to the git repository
   * @param propertyPath The path to the Java properties file
   * @return an error code
   */
  static native int generateGitProperties(String repoPath, String propertyPath, int options);
}
