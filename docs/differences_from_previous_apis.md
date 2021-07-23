# Differences From Previous APIs

Though JNX largely hews to the same overall concept of an NSF as the LSXBE and related APIs like ODA, there are some important conceptual differences.

## View Data

Reading data from views and folders is done by way of "collections" and queries upon them. This is based partially on the C API terminology and partially on more modern programmatic-query-based APIs.

View queries are based around specifying keys, directions, start points, and the explicit data to read. For example, to look up a category entry:

```java
DominoCollection view = database.openCollection("Lastname Birthyear Categorized");
CollectionEntry categoryEntry = view
    .query()
    .direction(Navigate.CURRENT)
    .readColumnValues()
    .readSpecialValues(SpecialValue.INDEXPOSITION)
    .startAtCategory(category)
    .firstEntry();
```

Or, to select a subset of documents based on a DQL query:

```java
DominoCollection sortView = database.openCollection("Lastname Firstname Flat");
sortView.resortView("lastname", Direction.Descending);

DQLTerm dql = DQL
    .item("Firstname")
    .isEqualTo("Nathan");

LinkedHashSet<Integer> sortedIdsOfDQLMatches = sortView
    .query()
    .select(
        SelectedEntries
            .deselectAll()
            .select(dql)
    )
    .collectIds(0, Integer.MAX_VALUE);
```

There exists a `View` interface, but it's specifically for working with the design of the view.

## Callbacks

There are several areas where the best way to perform an operation on a set of entities is via a callback function, such as `Database#forEachCollection` or `Document#forEachItem`. These are often implemented on top of similar callback structures at the C API level and are more efficient than other looping mechanisms available.

## Session Usernames

Like the C API, JNX does not have the inherent restrictions on session creation that the LSXBE classes do (particularly on the Java side with the agent security manager). By default, a newly-created `DominoClient` will use the name from the underlying Notes ID file from the active server or client runtime.

A `DominoClient` object can also be created with an arbitrary username:

```java
DominoClient fooClient = DominoClientBuilder.newDominoClient()
    .asUser("CN=Foo Bar/O=Baz")
    .build();
```

In this case, that username will be used to open any databases and perform related actions, and its access will be enforced locally and by trust-configured remote servers.

In practice, this should generally be done with a pre-verified username from the environment, such as the authenticated user in a servlet container or a configured "run as" user for a scheduled task.

It is also possible to perform password authentication against the environment's configured NSF-based directories during `DominoClient` creation:

```java
DominoClientBuilder.newDominoClient()
    .authenticateUser(someServer, username, password)
    .build();
```

This mechanism will throw a `DominoException` caused by a  `javax.naming.NamingException` subclass when the user doesn't exist or the password is incorrect.

## Dates/Times

There exists a `DominoDateTime` type, but the intent is to integrate heavily into the Java 8+ `java.time` system. To that end, `DominoDateTime` implements the `Temporal` interface and can be used anywhere a `Temporal` or `TemporalAccessor` is needed. For example:

```java
DominoDateTime dominoDt = someMethod();
OffsetDateTime dt = OffsetDateTime.from(dominoDt);
String output = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dominoDt);
```

`DominoDateTime` has convenience methods for `toOffsetDateTime`, `toLocalDate`, and `toLocalTime` to explicitly convert or extract date/time to JDK types. Note that, when the Domino value represents a time-only or date-only value, the inapplicable methods will throw exceptions.

It also has a `toTemporal` method that converts to the version of those that most closely matches the underlying data.

`DominoDateTime` does not include named time zone data, as this is not representable at the native layer. Instead, it stores just the time zone offset, and so it's a direct conceptual match for the `OffsetDateTime` type when it contains both a date and time, `LocalDate` when it's date-only, and `LocalTime` when it's time-only.

## Garbage Collection

JNX objects have their backing Notes handles and memory freed when they are collected by the Java garbage collector, and it is not the programmer's responsibility to keep track of them. It is good to call `DominoClient#close` when done (or use try-with-resources), but those clients can be long-lived and persist across threads.

Some interfaces, such as `Database`, also extend `AutoCloseable`, but that is for when you wish to more-explicitly manage your memory use, and are optional to call.