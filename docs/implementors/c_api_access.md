# C API Access

Notes C API access in the current implementation is performed using [JNA](https://github.com/java-native-access/jna), which allows access to native functions without writing a custom JNI library. JNA handles access to native memory (via its `Pointer` and `Memory` classes), structures (via `Structure` subclasses), and translating primitive data types in functions to their same-sized C equivalents.

This code is in the `domino-jnx-jna` module solely, to allow for potential future implementations.

## API Definition

The Notes functions used in the implementation are defined in the `com.hcl.domino.jna.internal.capi.INotesCAPI` interface, which contains Java equivalents of the C function prototypes. There is also a companion class in the same package, `NotesCAPI`, which is the primary entrypoint for accessing the API. The `NotesCAPI.get()` static method will, if needed, load the Notes dynamic libraries and return an appropriate implementation object for the current platform.

For primitive types, JNA maps same-sized Java primitives to their C equivalents, ignoring signed/unsigned state. For example, `short` maps to `WORD` and `int` maps to `DWORD`. On the Java size, when working with these values logically, it often makes sense to "upsize" them when they're unsigned in C, for example using `Short.toUnsignedInt(short)` when working with a WORD value from C.

For pointers, struct, and callback parameters, JNA has representations for each and they can be passed into the method definitions. For example:

```java
short NSFNoteCipherExtractWithCallback (DHANDLE.ByValue hNote, NotesBlockIdStruct.ByValue bhItem,
  int extractFlags, int hDecryptionCipher,
  NotesCallbacks.NoteExtractCallback pNoteExtractCallback, Pointer pParam,
  int Reserved, Pointer pReserved)
```

## Pointers and Memory

The JNA `Pointer` class represents any native pointer, while the `Memory` subclass allows you to allocate native memory that will be automatically freed when the Java object goes out of scope. In general, the `Memory` objects and `Structure` representations allocated in the JNA implementation don't need to be explicitly deallocated.

When it is useful to allocate and deallocate memory manually, we have a `DisposableMemory` class that exposes the `dispose()` method to immediately free the backing memory, without waiting for garbage collection.

Naming-wise, Java classes use `ByValue` for direct structure values and `ByReference` for struct pointers.

## Structures

Most of the structure definition classes in the JNA implementation were created using [JNAerator](https://github.com/nativelibs4java/JNAerator), which automates the process of parsing C header files and creating JNA representations of the structures. It's not required to use JNAerator - these classes can also be written by hand - but it's often very convenient to do so.

These classes generally have static methods for constructing new instances, either with newly-allocated native memory or with an existing backing pointer.

## Callbacks

JNA supports writing callback interfaces in a similar way to mapping to normal function prototypes, as long as the callback interface extends `Callback`. In JNX, these are kept in the `NotesCallbacks` containing interface. For example:

```java
public interface NotesCallbacks {
  interface ActionRoutinePtr extends Callback {
    short invoke(Pointer dataPtr, short signature, int dataLength, Pointer vContext);
  }
  
  // other callbacks follow
}
```

## Handles

`HANDLE`s and `DHANDLE`s have special representations within the `com.hcl.domino.jna.internal.gc.handles` package, which allow us to implement per-thread locking on handles and which cover the difference between 32- and 64-bit representations. These interfaces can be used as function parameters in `INotesCAPI`, and their values can be worked with via the methods in `LockUtil`, which coordinates per-handle locks. For example:

```java
short result = LockUtil.lockHandle(getAllocations().getDBHandle(), (dbHandleByVal) -> {
  return NotesCAPI.get().NSFDbNamedObjectEnum(dbHandleByVal, cCallback, null);
});
```

Here, `getAllocations().getDBHandle()` returns a `HANDLE`, while the `dbHandleByVal` object passed to the lambda is specifically a `HANDLE.ByValue`, needed for passing into the function call.

## Bitness

Since Notes on Windows is still 32-bit, we have to make accommodations for that. Most APIs are the same across both bitnesses and all platforms, but some require specialized versions, generally with handles passed to callbacks. For example:

```java
	short NSFDbNamedObjectEnum(HANDLE.ByValue hDB, NotesCallbacks.b64_NSFDbNamedObjectEnumPROC callback, Pointer param);
	short NSFDbNamedObjectEnum(HANDLE.ByValue hDB, NotesCallbacks.b32_NSFDbNamedObjectEnumPROC callback, Pointer param);
```

In addition, such callbacks should have sub-interfaces for 32-bit Windows specifically that also extend `StdCallCallback`. These are housed in `Win32NotesCallbacks`. For example:

```java
public interface Win32NotesCallbacks {
  interface NoteExtractCallbackWin32 extends NotesCallbacks.NoteExtractCallback, StdCallCallback {}
  
  // other callbacks follow
}
```

When calling so-afflicted functions, you can use JNA's `PlatformUtils.is64Bit()` to check for 64-bitness and, in the 32-bit branch of your code, `Platform.isWindows()` to check for Win32 specifically.

```java
if (PlatformUtils.is64Bit()) {
  NotesCallbacks.b64_NSFDbNamedObjectEnumPROC cCallback = (hDB, param, nameSpaceShort, nameMem, nameLength, rrv, entryTimeStruct) -> {
    // 64-bit callback code
  }
  // 64-bit C API call
} else if(PlatformUtils.is32Bit()) {
  NotesCallbacks.b32_NSFDbNamedObjectEnumPROC cCallback;
  if(PlatformUtils.isWindows()) {
    cCallback = (Win32NotesCallbacks.NSFDbNamedObjectEnumPROCWin32)(hDB, param, nameSpaceShort, nameMem, nameLength, rrv, entryTimeStruct) -> {
      // Windows/32-bit callback code
    };
  } else {
    cCallback = (hDB, param, nameSpaceShort, nameMem, nameLength, rrv, entryTimeStruct) -> {
      // Non-Windows 32-bit callback code (admittedly unlikely to ever see the light of day)
    }
  }
  // 32-bit C API call
}
```