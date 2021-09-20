# Domino JNX XPages Library

This module provides an OSGi-based XPages Library implementation that allows an XPages app to load and access JNX classes.

## Limitations

Currently, this is a very low-integration library: it does not provide any implicit variables or other runtime integration.

Additionally, it currently requires that you loosen your Java security policy with a rule such as:

```
grant {
    permission java.security.AllPermission;
};
```

The mechanism used to bundle the library also does not provide source or Javadoc to Designer.