package com.team2813.gradle;

import java.io.File;

public record FileLocations(
        File buildFile,
        File settingsFile,
        File expectedPropertyFile
) {}
