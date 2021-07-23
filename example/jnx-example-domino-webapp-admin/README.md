# JNX Example Domino Admin Webapp

This example demonstrates a single-module Domino webapp that makes use of JNX, registered as "/jnxadmin" on the server. The core structure is largely similar to `jnx-example-domino-servlet` but also demonstrates:

### JAX-RS 2.1

This app bundles RESTEasy to use a newer JAX-RS version than the ancient one provided by Wink. Specifically, this makes use of Server-Sent Events to do real-time streaming of console output to the browser.

### Console APIs

This uses `client.getServerAdmin().sendConsoleCommand(...)` to send a command to the target server. Currently, live watching of the server console output only works with the server that the app is running on. Additionally, it requires at least V12 beta 3 with this notes.ini property set:

`DEBUG_ALLOW_REMOTE_CON_SERVER=1`
