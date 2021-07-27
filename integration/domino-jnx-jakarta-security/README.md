# JNX Jakarta Security Integration

This module provides an `IdentityStore` implementation for the Jakarta Security 2.0 API, which is the `jakarta.*` namespace version. This identity store is marked as `@ApplicationScoped` in CDI and provides:

- Password authentication using the local Notes/Domino directory
- Group resolution using the local Notes/Domino runtime, based on the DN returned by the directory API


## Requirements

This assumes that your application is running in a container with active Jakarta Security and CDI services, providing the APIs and implementations.