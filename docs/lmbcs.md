# LMBCS Charset

The JNA implementation (as should future implementations) provides a Java NIO `Charset` to encode and decode LMBCS.

Example:

```java
Charset charset = Charset.forName("LMBCS"); // Or "LMBCS-native" to distinguish from ICU
byte[] encoded = expected.getBytes(charset);
String decoded = new String(encoded, charset);
```

This Charset is intended to support binary access to Composite Data and similar uses. In general, JNX API users do not have to be concerned with LMBCS encoding.

