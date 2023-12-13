---
layout: default
title: Overview
nav_order: 001
has_children: true
---
# Domino JNX

Domino JNX ("Domino Java NeXt") is a modern Java API for Domino based on using JNA to access Domino's C API, without using the Notes.jar classes. Its primary use is to support the Domino REST API, but it can also be used standalone as a general API for Notes and Domino.

Being a new implementation, this provides it with several advantages over the lotus.domino API:

- JNX heavily uses more-modern Java capabilities, such as streams, lambdas, better collections, and newer APIs like java.time
- Using the C API, JNX provides access to capabilities and efficiencies that are not present in lotus.domino, such as highly-detailed view traversal, ECLs, message queues that work with the Collections framework, and task progress monitors
- Java enums instead of integer-based flags
- [Detailed access to rich-text records](rich_text.md)
- Distribution through Maven Central, including source and Javadoc
- No concept of `recycle()`, as resources are closed automatically. Some objects have manual `close()` methods that can be used with try-with-resources, but this is meant for cases where strict efficiency is required

See [differences_from_previous_apis.md](differences_from_previous_apis.md) for some more details.

## Usage

JNX is provided as a Maven dependency, with the implementation available at:

[https://search.maven.org/artifact/com.hcl.domino/domino-jnx-jna](https://search.maven.org/artifact/com.hcl.domino/domino-jnx-jna)

This dependency will include the main `domino-jnx-api` module as well as its various third-party dependencies.

### Usage Outside Notes and Domino

When running JNX code from a context outside of a running Notes/Domino process, it is important to initialize the Notes environment. See [notes_environment.md](notes_environment.md) for more details.

### XPages Usage

The `domino-jnx-xpages` distribution, available from [https://github.com/HCL-TECH-SOFTWARE/domino-jnx/releases](https://github.com/HCL-TECH-SOFTWARE/domino-jnx/releases), provides access to JNX inside an XPages application. It is basic in what it does: it makes the API classes available in the NSF, but does not change the behavior of existing data access or provide implicit "session" or database variables.

### Expected Dependencies

In addition to the `domino-jnx-jna` implementation module, runtimes using this API must either implicitly provide
or otherwise include the following dependencies:

* The Jakarta XML Bind 3.0 API and an implementation, such as `com.sun.xml.bind:jaxb-impl:3.0.0`
* The Jakarta Activation 2.0 API and an implementation, such as `com.sun.activation:jakarta.activaton:2.0.0`
* The Jakarta Mail 2.0 API and an implementation, such as `com.sun.mail:jakarta.mail:2.0.0`

Additionally, the `domino-jnx-jsonb` adapter module expects that you have the Jakarta JSON 2.0 API and
an implementation, such as `org.eclipse:yasson:2.0.1`.

### OSGi Use

The JNX modules include OSGi metadata and can run in an environment such as Domino's HTTP stack. In addition to the core `domino-jnx-api`, `domino-jnx-commons`, and the `domino-jnx-jna` implementation, you should include the dependencies above, as well as the Glassfish OSGi resource locator, `org.glassfish.hk2:osgi-resource-locator:2.4.0`.

## Examples

The `example` directory in the main repository contains examples of using JNX in several contexts, including Domino-deployed OSGi components, traditional WAR-based webapps, and desktop SWT and GraalVM apps. These projects are not generally featureful examples, though some - such as `jnx-example-domino-webapp-admin` contain some features that could be useful in non-production cases, such as the live server console.