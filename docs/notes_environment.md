# Notes Environment Setup

Though not specific to Domino JNX, it's handy to know the requirements for having an active Notes environment to execute native Notes calls. How to do this and what the restrictions are depend on your OS of choice and the ID file in use.

These requirements specifically apply when you're running the code outside of Notes or Domino directly, such as via a command-line app or external webapp server. If you're in, for example, an OSGi servlet or Domino-run task, you don't have to worry about these.

### ID File Passwords

The easiest way to cover initializing the ID for external applications is to use a password-less ID file, such as a server or ID file created specifically for this use and not necessarily granted broad access elsewhere.

If your ID file has a password, in general the best way to handle that is to launch Notes with it and then go to File -> Security -> User Security, and then check the box marked "Don't prompt for a password from other Notes-based programs (reduces security)". Then, leave Notes open while you run your external program - it will be able to piggyback on the active Notes client session.

### Bitness

Your active JVM must match the bitness of your Notes runtime. In almost all cases - all Domino servers and the macOS Notes client - that should be 64-bit. However, as of V12, the Notes client on Windows is 32-bit, and so you should use a 32-bit JVM.

### JVM Type

As of V12, Notes on macOS must be loaded with an OpenJ9-based JVM, ideally the Java 8 release from AdoptOpenJDK (soon to be Adoptium). Loading the runtime with a HotSpot-based JVM will lead to a non-fatal NSD at launch and then a likely fatal crash later.

### Java Library Path

On all systems, you should set the `java.library.path` Java property to point to the Notes binaries. For example:

```sh
java -jar Foo.jar -Djava.library.path="/Applications/HCL Notes.app/Contents/MacOS"
```

Additionally, it may sometimes be necessary to also set `jna.library.path`. This requirement was observed on Windows when using a path with a space in it. For example:

```sh
java -jar Foo.jar -Djna.library.path="D:\Program Files\HCL\Domino" -Djava.library.path="D:\Program Files\HCL\Domino"
```

### Environment Variables

This applies to Linux and macOS, but usually not Windows, which picks up a lot of contextual information from, presumably, the registry.

There are a few environment variables that should be set to include the Notes/Domino binary directory.

On macOS, adjusting for your installation directory if necessary:

```
DYLD_LIBRARY_PATH=/Applications/HCL Notes.app/Contents/MacOS
Notes_ExecDirectory=/Applications/HCL Notes.app/Contents/MacOS
LD_LIBRARY_PATH=/Applications/HCL Notes.app/Contents/MacOS
```

On Linux, adjusting for installation and data directories:

```
LD_LIBRARY_PATH=/opt/hcl/domino/notes/latest/linux
Notes_ExecDirectory=/opt/hcl/domino/notes/latest/linux
PATH=$PATH:/opt/hcl/domino/notes/latest/linux:/opt/hcl/domino/notes/latest/linux/res/C
Directory=/local/notesdata
NotesINI=/local/notesdata/notes.ini
```

The `NotesINI` one is not a built-in variable but instead comes into play in the next section.

### Notes Runtime Initialization

To initialize the Notes runtime, you have to call the process and per-thread init and term methods. Using JNX, that can be done via:

```java
DominoProcess.get().initializeProcess(/* args here */);
DominoProcess.get().initializeThread();

// Later
DominoProcess.get().terminateThread();
DominoProcess.get().terminateProcess();
```

The arguments to `initializeProcess` are a vararg string array that are passed to [`NotesInitExtended`](https://www.mindoo.de/hcl/api901ref.nsf/61fd4e9848264ad28525620b006ba8bd/e151a8f6c224633d85255f7f00664516?OpenDocument).

On Windows, this can generally be empty.

On macOS, this should generally be a single string pointing to the Notes program directory, like:

```java
DominoProcess.get().initializeProcess(System.getenv("Notes_ExecDirectory"));
```

On Linux, this should also reference the desired notes.ini file (this is optional on macOS), and this is where the `NotesINI` environment variable comes in:

```java
String exec = System.getenv("Notes_ExecDirectory");
String ini = System.getenv("NotesINI");
DominoProcess.get().initializeProcess("=" + ini, exec);
```

### Properties to Skip True Initialization on Domino

JNX does checks internally to ensure that its app-level and thread-level initializers were called, in order to avoid hard crashes when calling down to the Notes API. However, when in fully-managed Domino contexts, such as an OSGi servlet or webapp, it is not necessary to actually call the initialization C functions and is best not to. For these situations, there are two Java properties to set to allow you to call the above methods to init JNX while not actually calling the Notes runtime functions:

```
System.setProperty("jnx.noterm", "true");
System.setProperty("jnx.noinittermthread", "true");
```

These can also be set ahead of time in the environment variables `JNX_NOINIT` and `JNX_NOINITTERMTHREAD`, respectively, with the values set to `1`.