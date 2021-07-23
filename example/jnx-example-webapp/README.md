# JNX Example Webapp

This application expects the following environment variables to be set to your Notes installation when using macOS:

```
Notes_ExecDirectory
LD_LIBRARY_PATH
DYLD_LIBRARY_PATH
```

For example, "/Applications/HCL Notes.app/Contents/MacOS".

## Authentication

The application is configured to require authentication for all requests and has an internal `IdentityStore` that validates
credentials using the active Notes runtime.

## Launching With Maven

To launch the application with the Liberty Maven plugin, set the `notes-program` Maven property to the location of your
Notes installation and then run `mvn liberty:run`. The application will be listening on port 9680.