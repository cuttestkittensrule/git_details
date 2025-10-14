# Git Details

A Gradle plugin for generating useful information about the state that git was in when the code was built.
This plugin generates a [java properties file][properties-file] with the information about the current git status, and adds it as a resource.

[properties-file]: <https://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html>

## Usage

You can add this plugin by putting it in your plugins declaration in your `build.gradle[.kts]` file.
Make sure you add the java plugin first, as this depends on the java plugin.

```groovy
plugins {
    id "java"
    id "com.team2813.gradle.git_details"
}
```

### Configuration

| Configuration         | Description                               | Default Value         |
|-----------------------|-------------------------------------------|-----------------------|
| resourceFilePath      | path of the generated resource file       | "git-info.properties" |
| gversionCompatibility | If gversion property names should be used | false                 |

### Generated Properties File Contents

#### Without gversion compatibility

| Property Name           | Value                                                                                                  | Type    |
|-------------------------|--------------------------------------------------------------------------------------------------------|---------|
| git_sha                 | commit SHA of the current checked out commit                                                           | String  |
| commit_date             | ISO formatted datetime that the checked out commit was made. Not present if the commit date is invalid | String  |
| build_date              | ISO formatted datetime that the code was built.                                                        | String  |
| has_uncommitted_changes | if there are uncommited changes                                                                        | boolean |
| branch_name             | The name of the checked out branch. Not present if a branch is not checked out                         | String  |

#### With gversion compatibility

| Property Name   | Value                                                                                                    | Type   |
|-----------------|----------------------------------------------------------------------------------------------------------|--------|
| git_sha         | commit SHA of the current checked out commit                                                             | String |
| git_date        | ISO formatted datetime that the checked out commit was made. Not present if the commit date is ambiguous | String |
| build_date      | ISO formatted datetime that the code was built.                                                          | String |
| build_unix_time | UNIX timestamp of the the build time                                                                     | long   |
| dirty           | if there are uncommited changes                                                                          | int    |
| branch_name     | The name of the checked out branch. Not present if a branch is not checked out                           | String |


## Prerequisites for building

### Install Rust

You need a rust toolchain in order to build the native code in the `native` subproject.
In order to install rust, it is recommended to install `rustup`, which you can install in your package manager of choice.
Make sure you add the default rust toolchain by running `rustup default stable` after you install `rustup`.

#### Brew

See <https://formulae.brew.sh/formula/rustup> for details.

After you run `brew install rustup` to install `rustup`, you need to add its bin folder to your `PATH`.
This can be done on terminal bootup by adding the following to your `.bashrc` or `.bash_profile`

```shell
export PATH="$PATH:$(brew --prefix rustup)/bin"
```

#### Without a package manager

To install rustup without a package manager, follow the instillation instructions on [rustup's website][rustup].
These methods are not preferred, as they do not perform integrity checks.

[rustup]: <https://rustup.rs>

## Building

In order to build the plugin, you just need to invoke the gradle `build` task.
On windows, this would be `.\gradlew.bat build` in the terminal.
On other operating systems, it would be `./gradlew build`.
