---
layout: default
title: User Directory API
nav_order: 006
parent: Overview
---
# User Directory API

The Domino directory API is accessible by calling `openUserDirectory(...)` on a `DominoClient` instance. The parameter is the server to target, or `null` for the current runtime.

## Querying

Directory querying can be done via the `query()` method on `UserDirectory`, which creates a query builder. This can be used like:

```java
UserDirectory dir = getClient().openUserDirectory(null);
Stream<List<Map<String, List<Object>>>> result = dir.query()
  .names("Joe Schmoe/SomeOrg")
  .items("InternetAddress")
  .stream();
```

This admittedly-complicated return type represents:

- One stream entry per queried name, containing...
- One list entry per queried namespace (by default one), containing...
- A map of queried items to item values

For example, to find the email address for a given user, you can do:

```java
String emailAddress = dir.query()
      .names("Joe Schmoe/SomeOrg")
      .items("InternetAddress")
      .stream()
      .findFirst()
      .map(queriedName ->
        queriedName.stream()
          .findFirst()
          .orElse(null)
      )
      .orElse(null);
```

## Convenience Methods

Since querying can be verbose for common cases, there are also convenience methods available on the `UserDirectory` class:

- `Map<String, List<Object>> lookupUserValue(String name, String... items)` allows you to look up item values for the given single user, returning the results from the first match