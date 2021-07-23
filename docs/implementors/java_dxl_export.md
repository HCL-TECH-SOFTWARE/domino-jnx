# DXL Export of Java Agents

Exporting of Java agents and similar design elements (such as web services and script libraries) is made complicated by the way the Notes internals do this specific type of export when not configured to use raw note format. Specifically, Notes calls back out to the running JVM to do the actual reading and exporting of the `%%source%%.jar` file.

The complications that arise related to this are:

- Notes determines the running Java environment based on thread-local normally set by `NotesThread.sinitThread` in LSXBE. This code stores a structure that contains, among other things, a `JNIEnv*` for the current thread.
- JNA does not provide a way to include `JNIEnv*` in a structure. The `com.sun.jna.JNIEnv` class can only act as a reference when used in a method signature.
	- This is currently worked around by creating a class named `lotus.domino.NotesThread` with same-named native methods to call.
- Both initial Java initialization done by `NotesThread` and the process of exporting the JAR files for some reason load references to a number of classes normally found within `Notes.jar` and `websvc.jar`, as well as the previously-standard Jakarta XML RPC API
- The actual file listing and extraction is done by a class named `lotus.notes.internal.IDEHelper`, whose `listJar` and `extractJar` are called explicitly by Notes via JNI

## Current Workaround

The current workaround for this involves the `domino-jnx-lsxbeshim` module, which contains stubbed-out versions of most classes determined to be loaded by the runtime. Though most are no-op stubs, two classes have actual use:

- `lotus.domino.NotesThread` contains `sinitThread` and `stermThread` methods that call the `native` methods to initialize and terminate the thread. This class doesn't do the remaining work covered by the normal `NotesThread`
- `lotus.notes.internal.IDEHelper` contains new implementations of `listJar` and `extractJar` to perform the tasks used by the runtime

## Known Limitations

- There remains a `NoSuchMethodError` stack trace logged to stderr during export. Unlike the previous instances of this, it is not accompanied by a log message indicating what that method is. It doesn't seem to impede export, though.
- Importing a non-raw Java agent will likely fail. Implementing `IDEHelper`'s remaining methods will likely go a long way towards this, but that would also require having a complete `lotus.domino` API on the classpath. This may be possible using NCSO.jar as a dependency, as it's redistributable and contains the classes used by an average agent
	- It's possible that `lotus.domino.JavaConnectLoader` would be involved, which would open a lot of flexibility, but that's speculation at the moment
- Actually _running_ any agents in a non-Notes/Domino environment will likely fail for lack of `Notes.jar` and `websvc.jar`