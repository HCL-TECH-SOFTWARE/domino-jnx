# Primary Implementation Tasks

These are the primary remaining tasks known to be required for JNX to be considered feature-complete.

### Design API

The design API is actively in progress. The remaining elements are:

- Views/Folders (read and write)
    - Most, but not all, of the view structures are represented
    - Once those exist, the column-info classes will need to wrap them
    - In turn, that will allow for moving columns around more readily
- Forms/subforms (write)
- Pages (read and write)
- Agents (write)
    - Java agents will potentially deserve consideration. It may or may not be in JNX's bailiwick to assist with compiling the JARs
- Script libraries (write)
- File resources (write)
- File-resource-like elements (read and write)
    - XPages Java elements
    - XPages JAR elements
    - Loose XPages files (.project, .settings, etc.)
    - XPages
    - Custom Controls
    - Themes
    - XPage translation bundles
    - Composite applications and related resources
- Stylesheets (write)
- Image resources (write)
- DB icon (some read and write)
- Shared actions (read and write)
- Shared columns (read and write)
- Framesets (read and write)
- About and Using documents (read and write)
- Applets (read and write)
- Outlines (read and write)
- Navigators (read and write)

[This file](https://github.com/OpenNTF/org.openntf.nsfodp/blob/2bfef49d943ba03e2c092851c6840b4cff41bcf1/nsfodp/bundles/org.openntf.nsfodp.commons.odp/src/org/openntf/nsfodp/commons/odp/OnDiskProject.java) from the NSF ODP project can be used as a general guide for elements expected to be found.

### Completion or Plan for Internal JVM Use and Required lsxbe Classes

As discovered when [exporting Java agents in non-raw format](https://github.com/FriendsOfKeep/domino-jnx/issues/135), there are parts in the core API that launch or use a JVM and make hard assumptions about the presence of several classes from Notes.jar and websvc.jar. There are two aspects of this that we need to come to a final determination about:

1) Registering the active `JNIEnv*` with the runtime. Currently, a feature branch has a workaround that uses a mock `NotesThread` implementation to initialize and terminate threads, which has the side effect of setting this. However, this path necessarily requires several class names from Notes.jar to be present to work, which is undesirable. Better would be if we can find a way to just set that `JNIEnv*` to at least avoid the required classes, or make this inclusion optional in some way, so that it's possible to use JNX for working needs without having this mock class or Notes.jar present.
2) Exporting and importing Java agents and similar elements via DXL requires additional classes beyond those initial ones, both to establish its classpath and to do the actual work of reading from and writing to JARs (see `lotus.notes.internal.IDEHelper`).

Beyond the borders of JNX, these troubles could be alleviated by cooperation with core, either lessening some of these hard requirements or coming up with a way to provide needed services to core without having to mimic class/method names directly.

### Documentation

Though most classes and methods in the API package have Javadoc, some of this is perfunctory and some is missing entirely.

It would also be good to expand the user-facing documentation in the `docs` directory and potentially make it part of a `mvn site` output.

### Test Suites

The test suites included in `domino-jnx-jna` and `it-domino-jnx` are primarily derived from either encountered individual bugs or testing harnesses for new features as developed. It would be good to also include real-world complex uses by people from outside core development to ensure that broad cases are covered.