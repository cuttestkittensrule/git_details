plugins {
    id("fr.stardustenterprises.rust.wrapper")
}

rust {
    release.set(true)

    command.set("cargo")

    cargoInstallTargets.set(false)

    targets {
        this += defaultTarget()

        register("linux-i686") {
            target = "i686-unknown-linux-gnu"
            outputName = "libgit_details.so"
        }

        register("linux-x86_64") {
            target = "x86_64-unknown-linux-gnu"
            outputName = "libgit_details.so"
        }

        register("win-x86_64") {
            target = "x86_64-pc-windows-gnu"
            outputName = "git_details.dll"
        }

        register("macos-x86_64") {
            target = "x86_64-apple-darwin"
            outputName = "libgit_details.dylib"
        }

        register("macos-aarch64") {
            target = "aarch64-apple-darwin"
            outputName = "libgit_details.dylib"
        }
    }
}
