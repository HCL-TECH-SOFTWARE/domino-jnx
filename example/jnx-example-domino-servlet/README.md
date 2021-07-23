# JNX Example Domino Servlet

This example demonstrates a single-module Domino servlet that makes use of JNX. There are
a few notable characteristics:

### `ServerStatusLine`

The `WebappInitializer` class is launched at HTTP start (by way of `WebappInitializerFactory`
being registered as a `com.ibm.xsp.adapter.serviceFactory` contributor service), and maintains an
addin-style status line and message queue while it's active. It will show up as `ExampleApp`
in `sh ta` on the server, will echo messages sent via `tell exampleapp foo`, and will display
a count of such messages in the status line.

### `maven-bundle-plugin`

`maven-bundle-plugin` is used to wrap the final JAR with OSGi metadata, and in particular
uses instructions that make sure it's compatible with the normal Domino HTTP stack.

Though it doesn't have any non-`provided` dependencies, any such ones would be packaged
into the bundle itself, to avoid having to worry too much about making sure they're
Domino-compatible.

### `p2-maven-plugin` and `p2sitexml-maven-plugin`

`p2-maven-plugin` is used to generate a p2 update site of the app itself and its JNX dependencies.

`p2sitexml-maven-plugin` is then used to generate a site.xml file, which is needed to import
the site with a category into an NSF-based update site.