# JNX Example Gluon Native

This example demonstrates a basic JavaFX app that can be compiled to a native application using Gluon.

## Prerequisites

Note: I'm not actually sure if it's important to install GraalVM first, but it's likely that it is.

Download GraalVM CE from:

[https://github.com/graalvm/graalvm-ce-builds/releases](https://github.com/graalvm/graalvm-ce-builds/releases)

The app targets Java 8, and the Java 11 builds work as well. Your GraalVM version should match the SDK used in this project. As of this writing, that is 21.2.0.

Then, follow the [instructions in the GraalVM documentation](https://www.graalvm.org/reference-manual/native-image/) for installing the `native-image` tool.

See [the GraalVM docs](https://www.graalvm.org/reference-manual/native-image/NativeImageMavenPlugin/) for more information about the Native Image Maven Plugin.

## Compiling

To compile the native image, run Maven with the `native-image` profile enabled:

```sh
$ mvn client:build
```

## Running

To run, ensure that your environment is set properly to point to your Notes environment. For example, on macOS:

```sh
$ export Notes_ExecDirectory="/Applications/HCL Notes.app/Contents/MacOS"
$ export LD_LIBRARY_PATH=$Notes_ExecDirectory
$ export DYLD_LIBRARY_PATH=$Notes_ExecDirectory
```

The JavaFX application can be run in a normal Java VM using:

```sh
$ mvn javafx:run
```

The compiled application can be run on most platforms using:

```sh
$ mvn client:run
```

Note: the above command will not work on macOS, as the subprocess spawned by `client:run` loses the surrounding `DYLD_LIBRARY_PATH` value. Instead, run the compiled application directly. For example:

```sh
$ target/client/x86_64-darwin/HCL\ Domino\ API\ Example\ Gluon\ Native\ App
```
