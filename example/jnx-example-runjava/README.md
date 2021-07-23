# JNX Example RunJava Addin

This example demonstrates an addin compatible with RunJava, without requiring the use
of Notes.jar.

This can be done by extending the `RunJavaAddin` class and implementing the `#runAddin` method,
which provides a server-ID `DominoClient`, a `ServerStatusLine` reflecting an entry in the server
task list, and a message queue that will work with `tell foo X`.