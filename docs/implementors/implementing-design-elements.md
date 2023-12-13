---
layout: default
title: Implementing Design Elements
nav_order: 007
parent: Contributors
---
# Implementing Design Elements

The entrypoint for accessing the database design is the `DbDesign` interface in the `domino-jnx-api` module, and the bulk of the implementation for that is in `AbstractDbDesign` inside the `domino-jnx-commons` moduke. Though there is a `JNADbDesign` class in the `domino-jnx-jna` module, that is just to provide a hook for an efficient C call to find elements - the bulk of the work should take place in `domino-jnx-commons` to allow for future portability.

## Entity Hierarchy

### Note-backed Entities

The `DesignElement` interface is the top-level interface for all note-based design elements, and its primary implementation is `AbstractDesignElement`. In turn, there are a handful of more-specific and "mixin" interfaces/abstract classes to narrow down common behavior among design elements:

- `NamedDesignElement` represents a design element that has a developer-specified name, usually stored in the `$TITLE` item. This means elements like Forms and Views as opposed to single-entity-only elements like the database script and the About and Using Documents. `AbstractNamedDesignElement` provides the base implementation for this
- `NamedFileElement` represents elements that are conceptually files, such as File and Image Resources, Composite App elements, and XPages-related elements. `AbstractNamedFileElement` provides the base implementation for this
- `XPageAlternativeElement` is a mixin interface to describe elements (Forms and Views) that have an "On web access, display XPage instead" option
- `XPageNotesAlternativeElement` is a mixin interface similar to above, but for elements with an "On client access, display XPage instead" option (admittedly, this is likely to remain confined to Forms and thus this interface may be removed in the future)
- `ThemeableClassicElement` is a mixin interface to describe elements (Forms, Views, Framesets) that have an option to configure their Themeing behavior (which allows you to use an XPages Theme file to specify a handful of largely-undocumented aspects of these elements)

Related entities generally share a common superinterface and abstract representation. For example, `Folder` and `View` extend from `CollectionDesignElement`, and all script-library types extend from `ScriptLibrary`.

### Component Entities

As needed, we create interfaces that don't extend `DesignElement` to cover individual parts of a larger design element, such as `Field` as present in forms and subforms, or `CollectionColumn` as present in views and folders.

Additionally, some elements use inner interfaces to provide access to large amounts of related attributes. For example, the `CollectionColumn` interface (which is in `com.hcl.domino.data` and not `com.hcl.domino.design` for historical reasons) contains a `SortConfiguration` interface that provides access to essentially what you see on the second tab of the Column properties dialog in Designer. Those settings don't have an existence separate from the column, but it makes sense to group them in this way to make the `CollectionColumn` interface more readable. In general, we don't want to go too far with this, but it makes sense when there is a logical grouping of more than a few properties.

### Utility Entities

- `ComputableValue` represents a value that in the Designer UI can be specified as either a static value or a formula. Examples include the name of the Twistie Image for a view column and the name of an included subform
- `SubformReference` represents an older specialized implementation of the above and will likely be removed in the near future

## Naming

In most cases, naming interfaces is straightforward, but some are difficult. For example, the shared interface of `View` and `Folder` is named `CollectionDesignElement` because "collection" describes both of those, but we wouldn't want to name it just `Collection` and leave it ambiguous and conflict with the Java Collections Framework. We also don't want to use `DesignCollection` (which would be analogous to `DesignAgent`, which in turn is so named because we already have an `Agent`) because the Design Collection is its own thing in Domino.

In another case, `GenericFormOrSubform` is so named because we don't have something better to describe both without just saying `Form`.

For implementations, we name abstract classes generally as `Abstract(interface name)`, like `AbstractNamedFileElement`. Complete implementations that exist in the `domino-jnx-commons` module are generally named `Default(interface name)` or `(interface name)Impl`, while implementations in `domino-jnx-jna` are generally named `JNA(interface name)`. The naming mix in `domino-jnx-commons` is likely to be resolved in one direction or the other in the future - for the moment, design elements are more commonly `(interface name)Impl`.

## Implementations

Subclasses of `AbstractDesignElement` are constructed with a `com.hcl.domino.data.Document` instance and use that for all or most of their access, and the high-level methods on that class should be preferred to using JNA directly when possible - and, indeed, it's best to put these in `domino-jnx-commons` until there's an explicit need to move them "down" to `domino-jnx-jna`. For example, `String getComment()` is implemented as:

```java
@Override
public String getComment() {
  return this.doc.getAsText(NotesConstants.FILTER_COMMENT_ITEM, ' ');
}
```

### Implementing Specialized Data Types

Though many of the fields in a design note are either basic types like TEXT or are composite data, some (particularly as you get to older elements) are unique formats. This is the case for Views and Folders with `$ViewFormat`, for example. These are identified in the NSF by distinct type codes, and the reading of them from the back-end document is done in `JNADocument`, in the `List<Object> getItemValue(String itemName, NotesBlockIdStruct itemBlockId, NotesBlockIdStruct valueBlockId, Pointer valuePtr, int valueLength)` method. This method contains a large if/then block to cover the handled data types, and so adding a new one can be done by adding to this block. The value returned in this is largely up to the implementor, but it should ideally be something that can be named in the `domino-jnx-api` or `domino-jnx-commons` modules, at least as a compatible interface.

For example, the block to read `$ViewFormat` is:

```java
else if (dataTypeAsInt == ItemDataType.TYPE_VIEW_FORMAT.getValue()) {
  DominoViewFormat viewFormatInfo = ViewFormatDecoder.decodeViewFormat(valueDataPtr,  valueDataLength);
  return Arrays.asList((Object) viewFormatInfo);
}
```

The `ViewFormatDecoder` does a bunch of work and produces a `DominoViewFormat` object, which is a class from `domino-jnx-commons`. In turn, this is used by `AbstractCollectionDesignElement`:

```java
  private synchronized DominoViewFormat readViewFormat() {
    if (this.format == null) {
      final Document doc = this.getDocument();
      this.format = (DominoViewFormat) doc.getItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM).get(0);
    }
    return this.format;
  }
```

The ideal is for at least an interface to be present in `domino-jnx-api` to account for cases where advanced API users may want to request these esoteric values, but that's not a high priority at the moment. For now, it should at least be something from `domino-jnx-commons` so that it's implementation-neutral.

### Constants

JNX admittedly doesn't have a coherent strategy for organizing its constants brought over from the C header files. We do strive to represent them generally in interfaces, but there are a number of them involved, both for historic reasons and to avoid slowness in Java IDEs.

As constants are needed, we take their definitions from C and put them in an applicable interface, aiming to match the type as they are used. For example:

```c
#define CAL_DISPLAY_CONFLICTS       0x0001  /* Display Conflict marks */
#define CAL_ENABLE_TIMESLOTS        0x0002  /* Disable Time Slots */
```

...converts to:

```java
/** Display Conflict marks */
short CAL_DISPLAY_CONFLICTS = 0x0001;
/** Disable Time Slots */
short CAL_ENABLE_TIMESLOTS = 0x0002;
```

There are several buckets currently in use for these:

- `com.hcl.domino.misc.NotesConstants` is the oldest and largest, containing a great many constants from across the API
- `com.hcl.domino.design.DesignConstants` is much smaller, and contains a handful of constants of use only in the Design API
- `com.hcl.domino.misc.ViewFormatConstants` contains constants used specifically for view-format reading, largely from viewfmt.h in the C SDK

There used to be a partial distinction between these in that `NotesConstants` was in `domino-jnx-jna` while the others were in `domino-jnx-commons`, but they've since all migrated up to `domino-jnx-api`.

### Structures

See [rich_text_implementation.md](rich_text_implementation.md) for a discussion of JNX's representation of most C structures as used in the `domino-jnx-api` module.

### Constant-Backed Enums

JNX has a mechanism for specifying "C-type" enumerations that are backed by constant values, which consists of the `INumberEnum<...>` interface and the `DominoEnumUtil` utility class. These can represent both "true" C enums with integer values as well as "bitmask" type multi-value enum fields used for things like "Flags" members of structs. For examples, see `CDExtField`, which has both cases.

For naming, the convention is to use a Java-style class name for the enum itself (like `Flag`) and then use a variant of the constant name without the leading disambiguating part for the actual enum constants. For example:

```java
enum HelperType implements INumberEnum<Short> {
  NONE(RichTextConstants.FIELD_HELPER_NONE),
  ADDRDLG(RichTextConstants.FIELD_HELPER_ADDRDLG),
  ACLDLG(RichTextConstants.FIELD_HELPER_ACLDLG),
  VIEWDLG(RichTextConstants.FIELD_HELPER_VIEWDLG);
  /* snip */
}
```

Though it's nice when the Java class name matches the internal constant prefix, it can be good in cases like the above to choose a different name either for disambiguation, existing-class-similarity, or general better-naming purposes.

These are useful outside of "struct" uses like this, though they have particular meaning there since they're used to determine the size of the overall structure. Some of the older classes in JNX have enums that could be `INumberEnum` but aren't yet, but this is just because we haven't yet gone back to adjust them.