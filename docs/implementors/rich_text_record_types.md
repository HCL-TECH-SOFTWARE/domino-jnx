# Rich Text Record Types

In the C API, rich text (composite data) records are primarily identified by `SIG_CD_xxx` flags, and the general equivalent to this in JNX is the `RecordType` enum. This enum maps those `SIG_CD_xxx` values to named enum values, as "areas" in which they might be found in a DB, and to interface representations in Java.

Though this enum has representations of nearly all CD records, it may not be exhaustive. There are cases where new enum entries may need to be added, either because we did not yet include them from the docs yet or because they are undocumented or new record types.

## Areas

The `RecordType.Area` enum is a JNX-ism that is not directly represented in the C API toolkit. This value is required, though, to account for situations where the same signature value is used for very-different structures depending on the rich text being read. For example, Navigators ("viewmaps" internally) use their own CD structures that can potentially use the same signatures as other records, but will not show up in the same record streams. This aspect is why the `getType()` method on `RichTextRecord` returns a `Set` of potential candidates.

When accessing a rich text stream, such as via `doc.getRichTextItem` or `RichTextUtil.readMemoryRecords`, the API user specifies which `Area` value to use when interpreting the stream, with `TYPE_COMPOSITE` being the default and by far the most common.

Individual `RecordType` enum values are bound to these areas via a single integer value or `int[]` in their constructor definitions. These numbers map to `Area` values as seen in the `RecordType.getRecordTypeForConstant` implementation.

## Interface Mappings

When a `RecordType` value does not have an associated implementation interface, then rich-text stream readers will emit `GenericBSIGRecord`, `GenericWSIGRecord`, and `GenericLSIGRecord` instances to provide generic memory access.

To bind an interface to a type, create an interface for it (see [rich_text_implementation.md](rich_text_implementation.md) for details) and then add that interface to the enum value constructor. When a stream processor sees this, it will create an instance of that interface (via a Proxy object) and provide that in the stream instead of the generic record type.