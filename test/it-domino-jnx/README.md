# JNX Integration Tests

## Creating New Test Classes

When the `it-domino-jnx` module is run as a test suite, it runs all annotated methods, so there's no need to enumerate classes specifically anywhere or adhere to a naming scheme.

It is almost always useful to extend `AbstractNotesRuntimeTest` or a subclass, as that abstract class provides init/term behavior for the runtime and threads. It also contains convenience methods for spawning and deleting temporary databases. For example:

```java
@Test
public void testFoo() throws Exception {
  withTempDb(database -> {
    // Work with database
  });
}
```

## Creating Tests With DXL-Based Test Data

The `AbstractNotesRuntimeTest` class also contains convenience methods for bulk-importing DXL files from a resource path (which will look in `src/test/resources`):

```java
@Test
public void testCreateDbReplica() throws Exception {
  withResourceDxl("/dxl/testCreateDbReplica", testDb -> {
    // ...
  });
}
```

When run, the `withResourceDxl` method will create a fresh temp DB, iterate through files in the `/dxl/testCreateDbReplica` directory, and import each one as DXL. The files can have one or more notes each.

These naming of the directories doesn't have any explicit requirements, but the convention is to place files within the "dxl" directory and then within a directory names after the test class or individual method.

It is safest to export the notes originally with "raw note format" DXL, to avoid any potential trouble with fidelity (though normal DXL will usually work fine). The `example/jnx-example-swt` app when run contains a menu option under "Apps" to launch a DXL exporter that has an option to set this flag.

### Design DXL Tests

The `TestDbDesign*` classes use a variant of the above method, but just import once for the whole class for efficiency's sake. This technique can be used in similar cases when there's a large amount of data to import, and isn't anything explicitly tied to the nature of being design-based.

## Creating Tests With NSF-Based Test Data

Though using DXL-based data is preferable for a number of reasons, sometimes it's useful to include NSFs in the test suite, such as when trying to handle cases of corrupt data.

The `AbstractNotesRuntimeTest` class also contains a method `withResourceNsf` that behaves similarly to the DXL version, but just makes a filesystem copy of the NSF for the test:

```java
@Test
public void testDocumentInvalidCreationDate() throws Exception {
  this.withResourceDb("/nsf/invalidtimedate.nsf", database -> {
    // ...
  });
}
```