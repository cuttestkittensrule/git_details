package io.github.cuttestkittensrule;

import java.io.File;

public record FileLocations(File buildFile, File settingsFile, File expectedPropertyFile) {}
