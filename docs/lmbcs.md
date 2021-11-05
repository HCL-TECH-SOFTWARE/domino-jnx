# LMBCS Charset

The JNA implementation (as should future implementations) provides a Java NIO `Charset` to encode and decode LMBCS. This can be retrieved using the `getLmbcsCharset` method on `NativeItemCoder`:

```java
Charset charset = NativeItemCoder.get().getLmbcsCharset();
byte[] encoded = expected.getBytes(charset);
String decoded = new String(encoded, charset);
```

This method has a variant that takes an `LmbcsVariant` enum value to control the handling of line breaks and null termination in the string.

Additionally, when JNX is in your system ClassLoader (for example, if an app using JNX was run directly with `java -jar`), you can use `Charset.forName`:

Example:

```java
Charset charset = Charset.forName("LMBCS"); // Or "LMBCS-native" to distinguish from ICU
```

This Charset is intended to support binary access to Composite Data and similar uses. In general, JNX API users do not have to be concerned with LMBCS encoding.

