# Working With MIME

There are two primary objects used to work with MIME: the `MimeReader` and `MimeWriter` both created via `getMime*` methods on `DominoClient`.

Both routes include raw `java.io.Reader`/`Writer` methods and methods that use [Jakarta Mail](https://eclipse-ee4j.github.io/mail/) objects.

## Reading MIME

The `MimeReader` class allows you to read MIME content from an item on a document either as an encapsulated Jakarta Mail `MimeMessage` object, written to a `java.io.Writer`, or as a processable `java.io.Reader`. Each method also contains a mechanism to specify which types of headers should be included, which may vary in mail documents.

## Writing MIME

Writing MIME can also be done either by providing a pre-formatted Jakarta Mail `Message` object or by the `java.io` classes.

This also provides the `convertToMime` method, which allows you to convert all Composite Data rich text in a document to MIME, in the same fashion as the mail router. MIME conversion involves more potential configuration than reading, and can vary greatly based on the configuration of your Notes/Domino runtime. This configuration can be done via `mimeWriter.createRichTextMimeConversionSettings()` and the methods on that settings object.

Of particular note is the `setMessageContentEncoding` method, which determines which of plain text, HTML, and images should be included in the final message.

## Jakarta Mail Version

The MIME classes use the Jakarta Mail 2.0 spec, which includes the namespace change from `javax.mail` to `jakarta.mail`. This dependency is expected to be provided by the app or container, either as a built-in component or brought in as a dependency.

