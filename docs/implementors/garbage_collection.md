---
layout: default
title: Garbage Collection
nav_order: 005
parent: Contributors
---
# Garbage Collection

The Commons package (`domino-jnx-commons`) contains capabilities to handle freeing back-end Domino references (such as handles and Notes-API-managed pointers) by way of `PhantomReference`s tracked by `com.hcl.domino.commons.gc.CAPIGarbageCollector`, as used by the JNA implementation. Each GC-participating API object, when created, registers itself there and keeps track of an `APIObjectAllocations` subclass instance (the phantom reference), which in turn keep tracks of native entities and provides a `dispose()` implementation that closes open handles and otherwise discards the backing memory.

For example, the `JNAIDTableAllocations` (which backs `JNAIDTable`) has a `HANDLE` property:

```java
public class JNAIDTableAllocations extends APIObjectAllocations<JNAIDTable> {
  private DHANDLE m_idTableHandle;
  // ...
  public DHANDLE getIdTableHandle() {
    return m_idTableHandle;
  }

  public void setIdTableHandle(DHANDLE m_idTableHandle) {
    this.m_idTableHandle = m_idTableHandle;
  }
}
```

The `JNAIDTable` class then uses that getter method whenever it wants to access the underlying ID table:

```java
return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
  return NotesCAPI.get().IDIsPresent(handleByVal, ((Integer)o));
});
```

The `dispose` implementation of `JNAIDTableAllocations` calls `IDDestroyTable`, marks the `HANDLE` wrapper object as disposed itself, and sets its instance property to `null`:

```java
@Override
public void dispose() {
  if (m_noDispose || isDisposed()) {
    return;
  }

  if (m_idTableHandle!=null) {
    LockUtil.lockHandle(m_idTableHandle, (handleByVal) -> {
      short result = NotesCAPI.get().IDDestroyTable(handleByVal);
      NotesErrorUtils.checkResult(result);
      //mark as disposed while lock is active
      m_idTableHandle.setDisposed();
      m_idTableHandle = null;
      return 0;
    });
  }
  
  m_disposed = true;
}
```

Seen here, some `*Allocations` implementations keep an internal `noDispose` property, but this should only be added when appropriate, such as when a handle might potentially be explicitly owned by Domino and thus shouldn't be destroyed by API consumers.

## Closing DominoClient Instances

The `DominoClient` interface extends `AutoCloseable`, and it is best to explicitly close these instances when they're known to no longer needed. Closing a client will dispose of all of its child objects and also remove the client-specific reference queue in `CAPIGarbageCollector`.

That said, `JNADominoClient` itself doesn't hold on to any native resources, and all of its children will participate in garbage collection, so it's safe to hold on to these instances for a while. For example, a `Servlet` implementation could create a `DominoClient` instance in its `init(...)` method and leave it open until the container calls `destroy()`.

## Disposing API Object Instances

In general, it's safe to leave disposing of API objects to the garbage collector, and most API interfaces don't have an explicit mechanism to dispose them.

For performance and memory-efficiency purposes, though, some interfaces also extend `AutoCloseable`, such as `Database`. In these instances, it's not required to close them when finished, but it can be more efficient to do so. Newly-created interfaces and objects shouldn't implement `AutoCloseable` unless the need arises.

When working with explicit JNA* implementations, it's good to explicitly dispose of objects when they're only short-lived. For example, if a method implementation creates a `JNAIDTable` as part of its implementation but does not return that to the user, you can call `dispose()` explicitly on it when done to keep memory and handle use more steady.

## Participating in Garbage Collection as an Extension

If you are writing a JNX extension (such as a `com.hcl.domino.admin.idvault.IdVaultTokenHandler` implementation), you can participate in JNX's garbage collection by including a dependency on the `domino-jnx-commons` module in addition to the core API and then:

1. Have your object extend `com.hcl.domino.commons.gc.BaseAPIObject`
2. Create a subclass of `com.hcl.domino.commons.gc.APIObjectAllocations` for your object (these two steps are inherently intermingled)
3. Construct your object with an `IAPIObject<?> parent` parameter, such as an existing `IGCDominoClient` implementation
    - Note: since these GC interfaces only exist in the "-commons" module, you should check to see if you're working with a compatible implementation by calling `getParentDominoClient` on one of the API objects and then `getAdapter(IGCDominoClient.class)` on the `DominoClient`

In your `APIObjectAllocations` instance, you are free to handle registering and closing resources however you would like. In general, the `JNAIDTableAllocations` example above can serve as a useful prototype. The most important thing is to make sure to keep track of any resources that need closing (`Pointer`s, etc.) in this allocations object, as the original implementation object (`JNAIDTable` in this example) will be inherently unavailable by the time your `dispose` method is called.