---
layout: default
title: Working With Rich Text
nav_order: 005
parent: Overview
---
# Rich Text

In JNX, rich text is dealt with by working with the underlying Composite Data representation. The `Document` class contains the method `getRichTextItem`, which returns an unmodifiable `List<RichTextRecord>`.

## Reading

### Traversing the List

This `List` lazily reads the CD records for all of the applicable items in the note. The most efficient way to traverse this list is by using stream and iterator methods. For example:

```java
for(RichTextRecord record : doc.getRichTextItem("Body")) { ... } 

doc.getRichTextItem("Body").stream().map(...).collect(...)

ListIterator<RichTextRecord> iter = doc.getRichTextItem("Body").listIterator()
```

### Record Subclasses

A subset of the available record types have more-specific interfaces that contain methods to access the record data easily. For example:

```java
for(RichTextRecord record : doc.getRichTextItem("Body")) {
  if(record instanceof CDText) {
    System.out.println("Found some text: " + ((CDText)record).getText());
  }
}
```

## Writing

There are two main ways to create and modify rich text: via item conversion or via writing CD records directly.

### Conversion

The `convertRichTextItem` method on the `Document` interface allows you to pass in one or more converter objects to process the stream of CD records. The API provides several standard converter implementations:

- `AppendFileHotspotConversion` to append a reference to an attachment on the document
- `RemoveAttachmentIconConversion` to remove the icon for a now-deleted attachment from a rich text item, if it is present

### Writing Records

The `createRichtextItem` method on the `Document` interface provides access to a `RichTextWriter` that lets you write CD records to an output writer. For example, to copy all non-text items from one item to another:

```java
List<RichTextItem> source = doc.getRichTextItem("Body");
try(RichTextWriter w = doc.createRichTextItem("Body2")) {
  source.stream()
    .filter(r -> !(r instanceof CDText))
    .forEach(w::addRichTextRecord);
}
```

It also contains convenience methods for copying in other rich text items or creating common records.

#### Encapsulated Writing Methods

`RichTextWriter` contains a number of methods for adding common types of data to a record set. For example:

- `addDocLink` appends a link to various Notes entities into the field
- `addImage` provides several ways to add an embedded image into the field
- `addAttachmentIcon` adds a reference to a document attachment to the field

#### Creating Records

`RichTextWriter` also contains a `addRichTextRecord` method that allows you to create a new record of a known type and process it. These records are defined by the interfaces in the `com.hcl.domino.richtext.records` package and contain methods for working with their structure members and variable-length data. For example:

```java
try(RichTextWriter w = doc.createRichTextItem("Body")) {
	w.addRichTextRecord(CDHotspotBegin.class, hotspotBegin -> {
		hotspotBegin.setHotspotType(CDHotspotBegin.Type.FILE);
		hotspotBegin.setFlags(EnumSet.of(CDHotspotBegin.Flag.NOBORDER));
		hotspotBegin.setFileNames(attachmentProgrammaticName, filenameToDisplay);
	});
}
```