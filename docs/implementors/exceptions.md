---
layout: default
title: Exceptions
nav_order: 004
parent: Contributors
---
# Exceptions

The core API uses the class `DominoException` as its main lower-level-API error representation, though it's rare that you will instantiate this class directly. It is a subclass of `RuntimeException` and so should not be declared in the `throws` portion of a method signature - this allows JNX code to work more smoothly in modern Java conventions like streams.

## JDK-Friendly Exceptions

When an exception case is something that it handled purely on the Java side and can be represented well as a "stock" exception, a normal `java.*` should be preferred.

For example, if a method has a parameter that must not be null, the best way to handle it is:

```java
public void someMethod(Object foo) {
	Objects.requireNonNull(foo, "foo cannot be null");
	// ...
}
```

`Objects.requireNonNull` will throw a `NullPointerException` in this case.

Similar should be done for other "normal" cases, like a number value that must be non-negative:

```java
public void someMethod(int bar) {
	if(bar < 0) {
		throw new IllegalArgumentException("bar must be non-negative");
	}
}
```

## `DominoException`s and `STATUS` values

The most common error condition we will run into is having a non-zero `STATUS` value returned by the Notes C API. For this case, the `com.hcl.domino.commons.util.NotesErrorUtils` class contains a `checkResult` method. This can be used directly on the return value of any `STATUS`-returning method. For example:

```java
NotesErrorUtils.checkResult(
	NotesCAPI.get().NSFFormulaDecompile(valueDataPtr, isSelectionFormula,
		rethFormulaText, retFormulaTextLength)
);
```

If the `STATUS` value (with `ERR_MASK` applied) resolves to 0, then this method does nothing. If it is non-zero, then it calls `OSLoadString` to find the corresponding error message and throws a new `DominoException` with that message and a stack trace pointing to the calling code.

Since that method throws the exception itself, it is important to handle any local native resources in a try/finally block. For example, if a call happened to take an ID table as a parameter that was created just for it, it should be destroyed in a finally block:

```java
DHANDLE.ByValue hTable = somethingThatCallsIDCreateTable();
try {
	NotesErrorUtils.checkResult(somethingThatUsesHTable());
} finally {
	NotesCAPI.get().IDDestroyTable(hTable);
}
```

### Subclasses and Special Handling

This method additionally has knowledge of several specialized `DominoException` subclasses, such as `ItemNotFoundException`. These exceptions can be caught the same way, but are useful for signaling to the programmer for specific situations without having them check the `getId()` value of the `DominoException`. New exception classes can be added to the `toNotesError(short result, String message)` method in `NotesErrorUtils` and the code calling `checkResult` does not need to know about them.

There are a handful of situations where it is useful to retrieve the `STATUS` value first before passing it to `checkResult`. For example, `JNADatabase#getDocumentById` returns an empty `Optional` when the result is `ERR_NOT_FOUND` and then otherwise passes the value along to `checkResult`:

```java
short result = LockUtil.lockHandle(allocations.getDBHandle(), (dbHandleByVal) ->
	NotesCAPI.get().NSFNoteOpenExt(dbHandleByVal, noteId, openOptions, rethNote)
);

if ((result & NotesConstants.ERR_MASK)==INotesErrorConstants.ERR_NOT_FOUND) {
	return Optional.empty();
}
NotesErrorUtils.checkResult(result);
```

## Non-`STATUS`-based and Other `DominoException`s

There are a few cases where exceptions are handled differently from above.

Two such cases are `FormulaCompilationException` and `LotusScriptCompilationException`, which are `DominoException` subclasses but carry extra information related to the specific cause of the failure. These exceptions are created manually based on code that checks the initial `STATUS` return value and then reads the detail "out" parameters to construct the exception.

In addition, there may be situations where a problem is reasonably represented in the `DominoException` category but isn't caused by a Notes API `STATUS` case. This is the situation for `ObjectDisposedException`, which arises when a JNX object has had its backend representation closed (e.g. via `dispose()` calling `IDDestroyTable`) but then the user tries to access it again. Guard code should check for this and throw a new `ObjectDisposedException`, though there is no `STATUS` code to go along with it.

## Documenting Exceptions

As mentioned above, these exceptions should _not_ be declared in method signatures, since they are `RuntimeException` subclasses. However, it is good form to document the potential for known common exception cases in Javadoc for the method. For example, from `DominoClient`:

```java
/**
 * Creates a new database on the target server with a given file path.
 * 
 * (snip)
 * @throws DominoException if the database already exists and {@code forceCreation} is {@code false}
 */
Database createDatabase(String serverName, String filePath, boolean forceCreation, boolean initDesign,
		Encryption encryption, DatabaseClass dbClass);
```

It is also good to document cases where a known subclass may be thrown, to signal to a downstream programmer that they might catch those more explicitly. From `Database`:

```java
/**
 * Opens a document in the database
 * 
 * (snip)
 * @throws DocumentDeletedException if the document has been deleted
 */
Optional<Document> getDocumentById(int noteId);
```

It's not important to cover every potential non-zero STATUS case, or even mention that such a thing might occur, since it's such a pervasive possibility. For example, `SERVER_NOT_RESPONDING` may arise in basically any remote-server case, but it's not important to document that in `Document#get`.