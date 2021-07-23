# Domino JNX

## Modern Domino Java API based on JNA access to Domino's C API

Java API to access Domino, loosely based on OpenNTF Domino API (ODA) ideas and DominoJNA. Does aim to be modern without backwards compatibility to existing Domino Java implementations

See the docs folder for details

## Expected Dependencies

In addition to the `domino-jnx-jna` implementation module, runtimes using this API must either implicitly provide
or otherwise include the following dependencies:

* The Jakarta XML Bind 3.0 API and an implementation, such as `com.sun.xml.bind:jaxb-impl:3.0.0`
* The Jakarta Activation 2.0 API and an implementation, such as `com.sun.activation:jakarta.activaton:2.0.0`
* The Jakarta Mail 2.0 API and an implementation, such as `com.sun.mail:jakarta.mail:2.0.0`

Additionally, the `domino-jnx-jsonb` adapter module expects that you have the Jakarta JSON 2.0 API and
an implementation, such as `org.eclipse:yasson:2.0.1`.

### OSGi Use

The JNX modules include OSGi metadata and can run in an environment such as Domino's HTTP stack. In addition to
the core `domino-jnx-api` and the `domino-jnx-jna` implementation, you should include the dependencies above, as well
as the Glassfish OSGi resource locator, `org.glassfish.hk2:osgi-resource-locator:2.4.0`.