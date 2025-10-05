# Prerequisites for building

## Install Rust

You need a rust toolchain in order to build the native code in the `native` subproject.
In order to install rust, it is recommended to install `rustup`, which you can install in your package manager of choice.
Make sure you add the default rust toolchain by running `rustup default stable` after you install `rustup`.

### Brew

See <https://formulae.brew.sh/formula/rustup> for details.

After you run `brew install rustup` to install `rustup`, you need to add it's bin folder to your `PATH`.
This can be done on terminal bootup by adding the following to your `.bashrc` or `.bash_profile`

```shell
export PATH="$PATH:$(brew --prefix rustup)/bin"
```

### Without a package manager

You can install rustup without a package manager, you can find instillation instructions on [rustup's website][rustup].
These methods are not preferred, as they do not perform integrity checks.

[rustup]: <https://rustup.rs>

# Building

In order to build the plugin, you just need to invoke the gradle `build` task.
On windows, this would be `.\gradlew.bat build` in the terminal, and it would be `./gradlew build` on other operating systems.


