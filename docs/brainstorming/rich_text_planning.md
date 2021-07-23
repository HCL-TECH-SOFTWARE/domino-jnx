# Rich Text Planning

2020-07-18 - Jesse Gallagher

The goals and pressures for a rich text/composite data API are:

- Provide close-to-direct access to CD records, which requires not masking their nature as much as lsxbe does
- Don't bleed the implementation details too much - ideally, even moderately-advanced use cases should not have to deal with e.g. LMBCS
- Provide escape hatch for truly custom use
  - This is in direct conflict with above, since dealing with bytes in memory also requires dealing with LMBCS, `LIST` structures, etc.
- Provide a reasonably-easy way to navigate and edit structures, such as finding a table or replacing text
- Not fall on its face when dealing with very large multi-item CD arrays, like file resources

## Overall Representation

### Idealized World: Queryable Tree

In this notion, rich text would be presented similarly to how DXL does it, where everything is not only nicely encapsulated, but is also presented as a tree structure.

**Pros**

- Self-enforcement of `BEGIN`/`END`, etc. structures
- Nodes can have querying methods for their children, like new-era DOM methods
- Matches the conceptual model that CD is resolved to

**Cons**

- Requires read-ahead, which can potentially cross item boundaries
- Would quickly run into show-stopping trouble working with large file resources

If implemented, this view should be optional and not the only way of dealing with CD records.

### Middle-Tier Abstraction: `List<CDRecord>`

In this notion, CD records would be presented as a Java collection of encapsulated objects, using `List` methods to largely replace "navigator" concepts.

**Pros**

- Very natural for Java use
- Parlays directly into Streams
- Is a pretty-direct map to the actual data
- The above "tree" API could be built on top of this
- The internal implementation would be pretty similar to the "navigator" concept we have now, plus allowing for performant index-based operations
  - An individual item's worth of records will max out at 32K(?), and so not problematic to keep in memory

**Cons**

- Some methods, like `size()`, would be abnormally expensive, especially with large files
  - That said, how often would one care about the count of CD records in a block?
- Would require clever internal caching of traversal
  - Specifically, it should know to not retain an entire large file resource in memory if the user happens to navigate to the end
  - It should retain some knowledge of the count and types of records in previously-visited items that have been flushed
- It'd really be best presented as a special sub-interface of `List`, to allow for the addition of RT-query methods, rather than having users be expected to write the same searching routines over and over
  - I suppose this could mostly be `.indexOf(Predicate<CDRecord> test)` and `.indexOf(Predicate<CDRecord> test, int fromIndex)`

## Escape Hatch

It will be important to provide some mechanism for developers to get down into the real dirt of it - ideally, they should be able to treat the whole thing or segments of it as a byte array in memory and manipulate it directly, as that sort of thing comes up much more frequently with CD than with other item types.

That said, it may not be the duty of the `domino-jna-api` module to provide that: the situation should come up infrequently enough that saying "use the JNA implementation directly" may suffice. This would hurt portability of user code if we add, say, a gRPC implementation, and that's not impossible to envision.

A route could be to use `ByteBuffer` instead of the `MemoryStructure` API we have now. While it would not cover LMBCS conversion, it would cover the other needs, it's part of the standard JDK, and it has direct support in JNA.

## Mapping Between API and JNA

Depending on how close-to-the-metal we want the user-facing API to be, it could make it difficult to avoid uncomfortable code duplication between the API and the JNA implementation, since `Structure` being a class means we'd have to either be interface-only in the API (limiting how closely we can represent the underlying code) or provide some sort of mapping between the two.

If we go the route of saying "if you want true native access, use the JNA implementation directly", that takes a lot of the pressure off - just having getters and setters for the struct members and variable-length data in the API will do fine.

If we embed more struct-type knowledge in classes in the API, it'd probably make sense to use our own annotations to indicate memory hints like member order and the like. Then, the JNA implementation could either have utility methods to copy between the two or, if we wanted to be unwise, a custom subclass of `Structure` that overrides the applicable methods. Having more of the implementation in the API project would make it more extensible by users, providing a structured middle ground between "use the methods we provide" and "here's a bag of bytes".