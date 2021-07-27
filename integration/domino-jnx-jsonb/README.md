# JNX Jakarta JSON Binding Adapter

This module provides a serializer and a deserializer for JNX `Document` objects using the Jakarta JSON Binding API. Specifically, it uses version 2.0, which is the `jakarta.*` namespace version.

## Usage

The serializer and deserializer must be registered with the `Jsonb` object at creation and are both configured with a builder pattern. Both classes are in the `com.hcl.domino.jnx.jsonb` package.

### Serializer

```java
Jsonb jsonb = JsonbBuilder.newBuilder()
    .withConfig(
      new JsonbConfig()
        .withSerializers(
          DocumentJsonbSerializer.newBuilder()
            .excludeItems(Arrays.asList("received"))
            .lowercaseProperties(true)
            .excludeTypes(Arrays.asList(ItemDataType.TYPE_COMPOSITE))
            .includeMetadata(true)
            .build()
        )
    ).build();
String jsonString = jsonb.toJson(doc);
```

- `excludeItems` allows for a denylist of case-insensitive item names to exclude from the output
- `includeItems` allows for an allowlist of items to include, instead of the default of including all items
- `lowercaseProperties` forces all output item names to be lower-cased. By default, the output JSON matches the item capitalization in the document
- `excludeTypes` allows for a denylist of `ItemDataType` values to exclude items by type
- `includeMetadata` includes a `@meta` property containing the note ID, UNID, creation date, modification date, accessed date, modified-in-file date, added-to-file date, and array of note classes. `false` by default

### Deserializer

```java
Jsonb jsonb = JsonbBuilder.newBuilder()
    .withConfig(
      new JsonbConfig()
        .withDeserializers(
          DocumentJsonbDeserializer.newBuilder(database)
            .detectDateTime(true)
            .build()
        )
    ).build();
Document doc;
try(InputStream is = getClass().getResourceAsStream("/somefile.json")) {
  doc = jsonb.fromJson(is, Document.class);
}
```

- `detectDateTime` causes the deserializer to check incoming string values against ISO date, time, and date/time formats and store them as `TIME` items in the document when found

## Requirements

This uses the `jakarta.*` version of the JSON-B API and expects that both the API and implementation will be provided by the app or container. This can be configured in a project with dependencies like these (or newer):

```xml
<dependency>
  <groupId>jakarta.json.bind</groupId>
  <artifactId>jakarta.json.bind-api</artifactId>
  <version>2.0.0</version>
</dependency>
<dependency>
  <groupId>org.eclipse</groupId>
  <artifactId>yasson</artifactId>
  <version>2.0.1</version>
</dependency>
```
