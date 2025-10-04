# Building

## Install Rust

### MacOS

See https://formulae.brew.sh/formula/rustup for details.

Install Rust build tools. If you use homebrew, you can run `brew install rustup`
then update your `.bashrc` to set the path:

```shell
export PATH="$PATH:$(brew --prefix rustup)/bin"
```

##  Install Rust ToolChain

Then run `rustup default stable` to install the stable toolchain.

