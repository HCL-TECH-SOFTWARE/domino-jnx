# JNX Example GraalVM Native Image

This example demonstrates a basic CLI app that can be compiled using GraalVM Native Image.

## Prerequisites

Download GraalVM CE from:

[https://github.com/graalvm/graalvm-ce-builds/releases](https://github.com/graalvm/graalvm-ce-builds/releases)

The app targets Java 8, and the Java 11 builds work as well. Your GraalVM version should match the SDK used in this project. As of this writing, that is 21.3.0.

Then, follow the [instructions in the GraalVM documentation](https://www.graalvm.org/reference-manual/native-image/) for installing the `native-image` tool.

See [the GraalVM docs](https://www.graalvm.org/reference-manual/native-image/NativeImageMavenPlugin/) for more information about the Native Image Maven Plugin.

## Compiling

To compile the native image, run Maven with the `native-image` profile enabled:

```sh
$ mvn clean package -Pnative-image
```

## Running

The packaged executable will be located in the `target` directory named `jnx-example-graalvm-native` (or `jnx-example-graalvm-native.exe` on Windows, probably). To run, ensure that your environment is set properly to point to your Notes environment. For example, on macOS:

```sh
$ export Notes_ExecDirectory="/Applications/HCL Notes.app/Contents/MacOS"
$ export LD_LIBRARY_PATH=$Notes_ExecDirectory
$ export DYLD_LIBRARY_PATH=$Notes_ExecDirectory
```
