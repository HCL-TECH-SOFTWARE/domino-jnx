# Color Values

Notes represents color values in several ways, and JNX represents them via enums and `MemoryStructure` instances.

## Standard Colors

The `StandardColors` enum represents the set of indexed colors that Notes uses largely for older components that pre-date the addition of "true" RGB color values.

The first sixteen of these colors are documented in the "colorid.h" file: `NOTES_COLOR_BLACK`, `NOTES_COLOR_WHITE`, and so forth. However, the colors themselves support at least 240 values (and are largely stored as an unsigned byte, and so may support 256 total). This extended color table is documented [in the AppDev Pack](https://doc.cwpcollaboration.com/appdevpack/docs/en/domino-richtext-reference.html#colortable).

The `StandardColors` enum is implemented as an `INumberEnum<Byte>`, as these colors are most-commonly represents as `BYTE` structure members. However, there are many cases where a structure will have a "color" member that's declared as `WORD` but nonetheless maps to one of these values. In such cases, it's important to still declare the `@StructureMember` as a `short`, but then add a second method to get the `StandardColors` values:

```java
@StructureGetter("DaySeparatorsColor")
short getDaySeparatorColorRaw();

default Optional<StandardColors> getDaySeparatorColor() {
  return DominoEnumUtil.valueOf(StandardColors.class, getDaySeparatorColorRaw());
}
```

## `COLOR_VALUE`

The `COLOR_VALUE` structure is used in many places to contain an RGB value and some flags, and is represented in JNX as the `ColorValue` structure interface. In principle, this is straightforward as far as its usage goes: the `Component1`, `Component2`, and `Component3` members correspond to RGB 0-255 values normally.

There are some cases, though, where it's important to check the `Flags` member to discern handling outside the structure. For example, the presence of the `HASGRADIENT` flag may indicate that an optional second `COLOR_VALUE` follows after this one in memory.

Additionally, there are some cases in the API where a `COLOR_VALUE` member of a structure exists but is unused. For example, the `VIEW_COLUMN_FORMAT2` structure has `COLOR_VALUE` components that are ostensibly used for colors in Notes R6 and above, but in practice are always 0 value even in R12.

## "Raw" Color Values

There are a handful of places in the API that use a `DWORD` to represent a color value. This amounts to a byte-order-specific array of four byte values representing the R, G, B, and spare values. JNX represents this using the `RawColorValue` structure interface. This type of storage is unnamed in the C API, but is called `RAW_COLOR_VALUE` in the structure definition in JNX.