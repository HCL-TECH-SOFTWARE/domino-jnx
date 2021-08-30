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
  // ...
}
```



Implementation notes:

- The order of the `@StructureMember` definitions must match exactly the order within the structure and must cover all data in order to get accurate offsets and data size
- `type` can be a numeric primitive type, an enumeration that implements `INumberEnum`, or another `MemoryStructure` interface
- When `unsinged` is `true`, the specified type should be the Java primitive that matches the physical size of the struct member. For example, a value that is represented as a non-bitfield `WORD` would be denoted here as `type=short.class, unsigned=true`. Accessor methods, though, should use the "upgraded" type, as described below
- The `name` value for both `@StructureDefinition` is currently only for developer reference, but must nonetheless be unique
- Array components can have their size expressed via the `length` property of `@StructureMember` and should have their `type` be the array form of their numeric or structure type, such as `short[].class` and `FontStyle[].class`
    - There are cases in the Notes API where there will fields marked as single-element arrays, like `WORD[1]`. In this case, it's just a bit less hassle to represent them as scalars and not arrays. They're also usually unused/reserved values, so the distinction doesn't programmatically matter
- The proxy implementation class, `MemoryStructureProxy`, has special support for `DominoDateTime` values in getters and setters when the member type is `OpaqueTimeDate`

For defining the structures, some common Notes types correspond in size to:

- DWORD = int
- WORD = short
- char = char
- DBID = TIMEDATE = OpaqueTimeDate (an existing JNX-specific structure corresponding to TIMEDATE)
- DBHANDLE = HANDLE = int
- BOOL = int
- NOTEHANDLE = DHANDLE = HANDLE = int

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

Accessor methods for numeric values that are unsigned in C should use a primitive type one level "upgraded" in Java. For example, if the member is defined as `type=short.class, unsigned=true`, then the getter and setter methods should use `int`. This doesn't apply when the underlying member is a "bitfield" or enumerated value and not treated as an actual number.

Multiple methods can reference the same struct member, as needed.

Setters must return the object itself.

Struct setters can also use `INumberEnum` values as parameters when the struct members are declared as a compatible primitive type. For example:

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

Embedded structure members (such as `COLOR_VALUE`/`ColorValue`) should not have a setter specified: since they permanently exist in memory, API users should use the getter for the structure and then use the setters on the structure itself.

#### Optionals

Getters for `INumberEnum` types can return an `Optional` of that type, to handle cases where the underlying value doesn't line up with any of the known values. This can be useful in general, but is particularly useful when none of the enum values are `0`: this allows for the getter method to avoid an exception when called with uninitialized data.

#### Bitfield Flags

Fields representing bit fields of flags should be marked as `bitfield = true` in their `@StructureMember` definition. Getters and setters for these types of fields should be designed to return `Set` and accept `Collection`, respectively. For example:

```java
@StructureGetter("Flags")
Set<Flag> getFlags();
  
@StructureSetter("Flags")
CDLayoutText setFlags(Collection<Flag> flags);
```

#### Mixing Primitives and Enums

`INumberEnum` values and their primitive equivalents can generally be mixed freely for getters and setters. For example, a struct member defined as a primitive can have a getter that returns a compatible `INumberEnum` value, and vice-versa. This can be useful for fields that are likely to contain errant or undocumented values.

#### Undocumented Flags in Bitfields

When setting a `Collection` of `INumberEnum` values to a struct member marked as a `bitfield`, `MemoryStructureProxy` will preserve any bits that are set but are not represented by an enum constant. For example, if the enums only represent values `0x0001`, `0x0010`, and `0x0100` but the existing value in the structure is `0x1111`, then setting an empty collection will store `0x1000`.

This also applies when a struct member that is otherwise a bitfield value contains a masked component that is a distinct type of value. Such values should be set and retrieved with independent default methods (see below).

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

### Fixed-Size String Members

Some structures, such as `CDFACE`, contain fixed-length `char[]` members instead of variable-length strings. These members should be represented in Java as `byte[]` values of the same length as in the structure. The `StructureSupport.readLmbcsValue` method can be used to read the value without the padding nulls. For example:

```java
@StructureGetter("Name")
byte[] getNameRaw();
    
default String getName() {
  return StructureSupport.readLmbcsValue(getNameRaw());
}
```

### String and Formula Variable Data

When the variable data of a structure (that is, contents beyond the defined fields) contains strings or compiled formula expressions, this can be accessed in a consistent way using `StructureSupport`, which contains methods for reading and writing these data types.

To read a string or formula, pass the object itself, the offset into the variable data, and the length of the data to read. For example:

```java
default String getFileHint() {
  return StructureSupport.extractStringValue(
    this,
    this.getServerHintLength(), // The total of all variable elements before this one
    this.getFileHintLength()    // the length of this element
  );
}
```

To write a new value, pass the above information, plus the new value and then a callback to write the value to a structure field. For example:

```java
default CDResource setFileHint(final String hint) {
  return StructureSupport.writeStringValue(
    this,
    this.getServerHintLength(),
    this.getFileHintLength(),
    hint,
    this::setFileHintLength     // Most structures have a specific member that houses this value
  );
}
```

Not all variable data has a special length member. In that case, you can pass in a no-op function for the last parameter, such as `(int len) -> {}`.

These writer methods will resize the memory of the record, creating a new copy in memory.

When working on Composite Data records, the writer methods will automatically update the `Length` field of the record's header.

### Generic Variable Data

Variable data after the fixed-size `struct` can be accessed by calling `getVariableData()` on the structure interface. For example, to read the text value of `CDTEXT` without using `StructureSupport`:

```java
// ...
public interface CDText extends RichTextRecord {
  // ...
  default String getText() {
    ByteBuffer buf = getVariableData();
    int len = buf.remaining();
    byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
  }
}
```

(Note: this specific case is now better done with `StructureSupport`, but other cases require specialized processing of this sort)

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

The implementation is based around the `MemoryStructureProxy` class, which uses Java's `java.lang.reflect.Proxy` capability to create objects with dynamic implementations of the methods defined in structure interfaces. The `MemoryStructureUtil` class contains static methods supporting proxy implementations and allowing for creating new ones.

The `forStructure(Class<I extends MemoryStructure> subtype, MemoryStructure structure)` static method creates a proxy instance to wrap the provided structure implementation, which will generally be either a lambda returning a `ByteBuffer` (for simple implementations) or an instance of `AbstractCDRecord`.

The `newStructure(Class<I extends MemoryStructure> subtype, int variableDataLength)` static method allocates a new memory buffer of size equal to the combined structure size of `subtype`'s members and `variableDataLength`, and then returns a proxy backed by that.

Methods annotated by `@StructureGetter` and `@StructureSetter` will be processed by the proxy, which derives the mechanism to extract or set the values in the internal byte buffer based on the structure definition and the definition of any inner structures. `default` methods will be executed backed by the wrapped objects. Any real implementation methods, such as those on `AbstractCDRecord`, will be passed along to the wrapped object itself.