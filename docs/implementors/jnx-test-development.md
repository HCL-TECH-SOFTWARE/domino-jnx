---
layout: default
title: Test Suites and Coverage
nav_order: 999
parent: Contributors
---
# JNX Test Suites and Coverage

Though JNX isn't based on Test-Driven Development by that capitalized name, it uses tests heavily for existing and new features. There are several sets of tests with different purposes, with the last being the most important and the location of most feature tests.

All tests are written using JUnit 5 and run either using `maven-surefire-plugin` (such as in a Docker-based build) or during development via Eclipse's JUnit runner.

## Test Suite Locations

### `domino-jnx-api`

The `domino-jnx-api` Maven module contains a handful of tests, but very rarely grows new ones as there's little functionality to test. This is a place to add any tests for the handful of utilities there and for "sanity checks" such as `TestStructAnnotations`, which ensures that the `MemoryStructure` definitions that the programmer wrote don't have knowable problems.

Tests here can't expect the presence of an actual initialized Notes runtime.

### `domino-jnx-commons`

The `domino-jnx-commons` module has tests similar to those in `domino-jnx-api`: simple checks to make sure utility methods work as expected.

Tests here can't expect the presence of an actual initialized Notes runtime.

### `domino-jnx-jna`

The `domino-jnx-jna` module has a moderate amount of tests, partly by virtue of being the original location for all tests and partly in order to test things that are specific to the JNA implementation, like the specifics of the garbage collector. Though those tests are important to run, it's fairly rare that it's best to add new tests here.

Tests here _can_ expect the presence of an actual initialized Notes runtime and can make all Notes API calls. The `AbstractJNARuntimeTest` is a useful base class for suites with utility methods to manage a `JNADominoClient` instance and build/tear down temp databases, optionally importing DXL resources.

### `test/it-domino-jnx`

The `it-domino-jnx` module is the primary location of test suites for all features, and where almost all new tests should go as you develop.

This is structurally similar to `domino-jnx-jna`'s tests: it expects the availability of an initialized Notes runtime and it contains `AbstractNotesRuntimeTest` with similar utility methods for providing a `DominoClient` and managing temp databases.

Though this module depends on `domino-jnx-jna`, that is only out of necessity for the classpath, and tests in here should _not_ assume the presence of the JNA implementation specifically or make calls to classes in that module. Ideally, tests would also not assume the presence of `domino-jnx-commons` either, but there may be situations where it is useful to test `-commons` behavior with an active Notes runtime. This should be rare at most, though.

## Running Tests

Due to the way the Notes runtime on various platforms works, actually running the tests can be tricky. The main requirements are to set the Java system properties and environment variables in the way described in [notes_environment.md](../notes_environment.md). Additionally, the "README-tests.md" file in the root of the repository contains information about environment variables to set to execute tests that rely on local or remote environment configuration.