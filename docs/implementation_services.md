---
layout: default
title: ServiceLoader Services
nav_order: 007
parent: Overview
---
# Implementation ServiceLoader Services

## Obligatory Services

JNX implementations are expected to provide a number of single `ServiceLoader`-type services, including:

- `com.hcl.domino.DominoClientBuilder`
- `com.hcl.domino.DominoProcess`
- `com.hcl.domino.formula.FormulaCompiler`
- `com.hcl.domino.naming.Names`
- `com.hcl.domino.richtext.structures.MemoryStructureWrapperService`
	- A default implementation of this is available in domino-jnx-commons as `com.hcl.domino.commons.richtext.structures.DefaultMemoryStructureWrapperService` but is not registered as a service in that project
- `java.nio.charset.spi.CharsetProvider` providing `Charset`s for "LMBCS" and "LMBCS-native"

## Optional/Extension Services

Additionally, there are several non-obligatory service points to provide specific capabilities. Two of them allow for contributing type conversion for the `get(name, type, defaultValue)` methods on collection entries and documents:

- `com.hcl.domino.data.CollectionEntryValueConverter`
- `com.hcl.domino.data.DocumentValueConverter`

There are a number of implementations of these in domino-jnx-commons in the `com.hcl.domino.commons.converters` package.

Note: it is expected that API implementations provide a `DocumentValueConverter` that can return an item value as a `java.nio.ByteBuffer`. Such a converter does not have to accept a `ByteBuffer` to write, however. The JNA implementation provides `com.hcl.domino.jna.internal.converters.ByteBufferDocumentValueConverter`.

Additionally, there are two extension services for contributing to user authentication and ID lookups:

- `com.hcl.domino.security.CredentialValidationTokenHandler` allows for arbitrary authentication mechanisms that resolve to a distinguished name, which be used in `DominoClient` to just validate or `DominoClientBuilder` to create a client with that name
- `com.hcl.domino.admin.idvault.IdVaultTokenHandler` allows for arbitrary mechanisms that find `UserId` objects for a given token