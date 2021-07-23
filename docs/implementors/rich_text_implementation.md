# Rich Text Internal Implementation

The rich text public API is based on exposing the CD records to the user in a way that is not tied to JNA specifically.

This is immediately accomplished through the use of `ByteBuffer`s: in the JNA implementation, records return native buffers to point to their data, which is then available to the developer.

## Struct Definitions

CD records and their associated `struct`s are represented as sub-interfaces of `MemoryStructure`, with CD records extending the `RichTextRecord` sub-interface. The layout of the in-memory `struct` is defined using the `@StructureDefinition` annotation. For example:

```java
@StructureDefinition(
  name="CDIMAGEHEADER",
  members={
    @StructureMember(name="Header", type=LSIG.class),
    @StructureMember(name="ImageType", type=CDImageHeader.ImageType.class),
    @StructureMember(name="Width", type=short.class, unsigned=true),
    @StructureMember(name="Height", type=short.class, unsigned=true),
    @StructureMember(name="ImageDataSize", type=int.class, unsigned=true),
    @StructureMember(name="SegCount", type=int.class, unsigned=true),
    @StructureMember(name="Flags", type=int.class),
    @StructureMember(name="Reserved", type=int.class)
  }
)
public interface CDImageHeader extends RichTextRecord {
  // ...
}
```

```java
@StructureDefinition(
  name="FONTID",
  endianSensitive=true,
  members={
    @StructureMember(name="Face", type=FontStyle.StandardFonts.class),
    @StructureMember(name="Attrib", type=FontStyle.Attribute.class, bitfield=true),
    @StructureMember(name="Color", type=FontStyle.StandardColors.class),
    @StructureMember(name="PointSize", type=byte.class, unsigned=true)
  }
)
public interface FontStyle extends MemoryStructure {
```



Implementation notes:

- The order of the `@StructureMember` definitions must match exactly the order within the structure and must cover all data in order to get accurate offsets and data size
- `type` can be a numeric type, an enumeration that implements `INumberEnum`, or another `MemoryStructure` interface
- When `unsinged` is `true`, the specified type should be the Java primitive that matches the physical size of the struct member. For example, a value that is represented as a non-bitfield `WORD` would be denoted here as `type=short.class, unsigned=true`. Accessor methods, though, should use the "upgraded" type, as described below
- The `name` value for both `@StructureDefinition` is currently only for developer reference
- Bitfield/flags-tyle fields can be denoted with `bitfield=true`, and can then be accessed in getters with `Set<EnumClass>` and setters with `Collection<EnumClass>` and subclasses
- Array components can have their size expressed via the `length` property of `@StructureMember` and should have their `type` be the array form of their numeric or structure type, such as `short[].class` and `FontStyle[].class`
- The proxy implementation class, `MemoryStructureProxy`, has special support for `DominoDateTime` values in getters and setters when the member type is `OpaqueTimeDate`

### Getters and Setters

Getters and setters should be annotated with `@StructureGetter` and `@StructureSetter` and should have types that match those defined in the `@StructureMember` annotation.

```java
// ...
public interface FontStyle extends MemoryStructure {
  @StructureGetter("PointSize")
  short getPointSize();
  
  @StructureSetter("PointSize")
  FontStyle setPointSize(int size);
  
  // ...
}
```

Accessor methods for numeric values that are unsigned in C should use a primitive type one level "upgraded" in Java. For example, if the member is defined as `type=short.class, unsigned=true`, then the getter and setter methods should use `int`. This doesn't apply when the underlying member is a "bitfield" or "type" member and not treated as an actual number.

Setters can either return the object itself or be declared as `void`. Multiple methods can reference the same struct member, as needed.

Struct setters can also use `INumberEnum` values are parameters are return values when the struct members are declared as a compatible primitive type. For example:

```java
@StructureDefinition(
  name="ODS_ASSISTFIELDSTRUCT",
  members={
    // ...
    @StructureMember(name="wOperator", type=short.class),
    // ...
  }
)
public interface AssistFieldStruct extends MemoryStructure {
  enum ActionByField implements INumberEnum<Short> {
    // ...
  }
  @StructureSetter("wOperator")
  AssistFieldStruct setActionOperator(ActionByField actionByField);
}
```

### Default Methods

Default methods in interfaces are supported to allow for specialized operations. For example:

```java
// ...
public interface FontStyle extends MemoryStructure {
  // ...
  @StructureGetter("Attrib")
  Set<Attribute> getAttributes();
  
  @StructureSetter("Attrib")
  FontStyle setAttributes(Collection<Attribute> attributes);
  
  default FontStyle setUnderline(boolean b) {
    Set<Attribute> style = getAttributes();
    style.add(Attribute.UNDERLINE);
    setAttributes(style);
    return this;
  }
  
  default boolean isUnderline() {
    return getAttributes().contains(Attribute.UNDERLINE);
  }
}
```

### Variable Data

Variable data after the fixed-size `struct` can be accessed by calling `getVariableData()` on the structure interface. For example, to read the text value of `CDTEXT`:

```java
// ...
public interface CDText extends RichTextRecord {
  // ...
  default String getText() {
    ByteBuffer buf = getVariableData();
    int len = buf.remaining();
    byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, Charset.forName("LMBCS-native")); //$NON-NLS-1$
  }
}
```

This example also demonstrates the use of the LMBCS charset provider, which allows for implementation-neutral handling of LMBCS text.

Variable data can be modified by calling the `resizeVariableData(int)` method. This method resizes the backing `ByteBuffer` and copies the existing data into it, up to the new size. It also sets the `Length` value of the CD record's header to match its new length.

For example, to set a new value for `CDTEXT`:

```java
// ...
public interface CDText extends RichTextRecord {
  // ...
  default CDText setText(String text) {
    byte[] lmbcs = text.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
    resizeVariableData(lmbcs.length);
    ByteBuffer buf = getVariableData();
    buf.put(lmbcs);
    return this;
  }
}
```

Note that it is important to set any secondary length values in the structure to the appropriate new size. For example, the `CDIMAGESEGMENT` structure contains data- and segment-size properties in addition to the overall CD record header `Length` property.

## Implementation

The implementation is based around the `MemoryStructureProxy` class, which uses Java's `java.lang.reflect.Proxy` capability to create objects with dynamic implementations of the methods defined in structure interfaces.

The `forStructure(Class<I extends MemoryStructure> subtype, MemoryStructure structure)` static method creates a proxy instance to wrap the provided structure implementation, which will generally be either a lambda returning a `ByteBuffer` (for simple implementations) or an instance of `AbstractCDRecord`.

The `newStructure(Class<I extends MemoryStructure> subtype, int variableDataLength)` static method allocates a new memory buffer of size equal to the combined structure size of `subtype`'s members and `variableDataLength`, and then returns a proxy backed by that.

Methods annotated by `@StructureGetter` and `@StructureSetter` will be processed by the proxy, which derives the mechanism to extract or set the values in the internal byte buffer based on the structure definition and the definition of any inner structures. `default` methods will be executed backed by the wrapped objects. Any real implementation methods, such as those on `AbstractCDRecord`, will be passed along to the wrapped object itself.