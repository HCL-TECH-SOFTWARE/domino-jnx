---
layout: default
title: JSON Handling
nav_order: 002
parent: Overview
---
# JSON Handling

JNX provides the `com.hcl.domino.json.JsonSerializer` type to allow serializing `Document` objects to JSON.

## Implementations

The actual implementation of this is reliant on using a secondary module for your environment. Currently, the implementing modules are:

- `domino-jnx-vertx-json` - Uses the Vert.x JSON classes and assumes that you have `vertx-core` present in your project. The `getObject` method emits a Vert.x `JsonObject` instance
- `domino-jnx-jsonb` - Uses JSON-B classes and assumes you have the Jakarta EE 9+ `jakarta.json.bind-api` and `jakarta.json.json-api` modules and an implementation present in your project. The `getObject` method emits a `String`

## Usage

### Serialization

```java
String jsonString = JsonSerializer.createSerializer()
  .excludeItems(Arrays.asList("received"))
  .lowercaseProperties(true)
  .excludeTypes(Arrays.asList(ItemDataType.TYPE_COMPOSITE))
  .includeMetadata(true)
  .richTextConvertOption(HtmlConvertOption.XMLCompatibleHTML, "0")
  .richTextConvertOption(HtmlConvertOption.DisablePassThruHTML, "1")
  .customProcessor("Foo", (doc, itemName) -> "Custom Value")
  .toJsonString(doc);
```

- `excludeItems` allows for a denylist of case-insensitive item names to exclude from the output
- `includeItems` allows for an allowlist of items to include, instead of the default of including all items
- `lowercaseProperties` forces all output item names to be lower-cased. By default, the output JSON matches the item capitalization in the document
- `excludeTypes` allows for a denylist of `ItemDataType` values to exlude items by type
- `includeMetadata` includes a `@meta` property containing the note ID, UNID, creation date, modification date, accessed date, modified-in-file date, added-to-file date, and array of note classes. `false` by default
- `booleanItemNames` specifies which items to consider boolean values. This should be used in concert with `booleanTrueValues`
- `booleanTrueValues` specifies which item values (of various types) should be considered `true` when found in an item named in `booleanItemNames`
- `dateRangeFormat` allows for customization of date/time ranges:
  - `ISO` (the default) concatenates the two components with a `/`
  - `OBJECT` creates an inner JSON object with `from` and `to` properties
- `richTextConvertOption` allows specification of "HTMLOptions" values for conversion of rich text to HTML
- `customProcessor` allows specification of a method that will be called when encountering a named item instead of normal processing. The `BiFunction` provided as the second parameter is called with the context document and current item name and should produce a JSON-compatible Java object (e.g. `String` or `Map<String, Object>`)

### Deserialization

Deserialization of JSON to a document has two modes: writing to an existing document or creating a new in-memory document in a database:

```java
Document doc = JsonDeserializer.createDeserializer()
  .target(database)
  .booleanValues("Y", "N")
  .dateTimeItems(Collections.singleton("Posted"))
  .fromJson(json);
```

```
Document doc = fetchDocumentFromSomewhere();
JsonDeserializer.createDeserializer()
  .target(doc)
  .removeMissingItems(true)
  .detectDateTime(true)
  .customProcessor("foo", (value, propName, doc) -> { doc.replaceItemValue("Foo", "Bar"); })
  .fromJson(json);
```

- `booleanValues` allows you to specify how JSON boolean literals will be stored in the document. This defaults to `1` and `0`
- `dateTimeItems` allows you to specify which items in the JSON should be interpreted as date/time values. These items will be interpreted as ISO 8601 strings or arrays of those strings, and the deserializer will throw an exception if it encounters an unparseable value
- `removeMissingItems` applies only when targeting an existing document and allows you to have items in the document not found in the JSON removed, other than "Form" and items with names beginning with "$"
- `detectDateTime` allows you to specify whether the deserializer will attempt to detect ISO-format string values and convert them as date/time values in the destination document
- `customProcessor` allows specification of a method that will be called when encountering a named property (case-sensitive) instead of normal processing. The `CustomProcessor` provided as the second parameter is called with the JSON value, the property name, and the target document