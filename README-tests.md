# Running Tests

To execute, all tests must be able to initialize a Notes runtime, and the requirements are different across platforms.

### All Platforms

On all platforms, libnotes (in its platform-specific variant) must be available in the Java library path. The way the path is consulted varies across platforms, but it is best to put the path to the Notes/Domino program directory in the `PATH` environment variable.

### macOS

On macOS, the following additional environment variables must be set:

- `DYLD_LIBRARY_PATH` must contain the Notes executable directory
- `LD_LIBRARY_PATH` must contain the Notes executable directory
- `Notes_ExecDirectory` must contain the Notes executable directory

Note: on macOS, passing environment variables into the Maven environment is exceedingly difficult. The most-reliable way to run tests is via Eclipse's JUnit runner with these variables set in the "Environment" tab.

### Linux

On Linux, the following additional environment variables must be set:

- `LD_LIBRARY_PATH` must contain the Notes executable directory
- `Notes_ExecDirectory` must contain the Notes executable directory
- `NotesINI` must point to the path of the active notes.ini file to use



## Environment-Driven Tests

Several test cases require environment variables to be set in order to successfully run a test in a given setup.

### TestClientBasics

The `testPingServer` test in `TestClientBasics` uses one property to determine if it should run:

- `PING_SERVER` should contain the name of a server that can be successfully pinged by the test runtime

### TestIdVault

The `TestIdVault` class requires two properties to test accessing an ID file:

- `IDFILE_PATH` set to point to a valid Notes ID file on the filesystem
- `IDFILE_PASSWORD` set to the password of that ID file

Additionally, it requires three properties to test accessing an ID from an ID Vault:

- `IDVAULT_USERNAME` set to a username to look up in the Vault
- `IDVAULT_PASSWORD` set to the password for that user
- `IDFAULT_IDVAULTSERVER` set to the server name of the Vault server

### Name Lookup Tests

`TestValidateCredentials#testValidateCredentials` and several other name-related test cases requires two properties and uses a third to test user credentials:

- `VALIDATE_CREDENTIALS_SERVER` optionally set to a server to validate credentials against
- `VALIDATE_CREDENTIALS_USER` must contain a user name to look up
- `VALIDATE_CREDENTIALS_PASSWORD` must contain that user's correct password

The `TestValidateCredentials#testValidateCredentialsBadPassword` test case requires two properties and uses a third to test user credentials:

- `VALIDATE_CREDENTIALS_SERVER` optionally set to a server to validate credentials against
- `VALIDATE_CREDENTIALS_USER` must contain a user name to look up
- `VALIDATE_CREDENTIALS_BADPASSWORD` must contain an incorrect password for that user

The `TestUserDirectory#testGetDirectoryPathsRemote` also uses `VALIDATE_CREDENTIALS_SERVER`, while `#testLookupEmail` uses that plus:

- `USER_DIRECTORY_EMAILUSER` must contain a username to look up in the `VALIDATE_CREDENTIALS_SERVER` directory
- `USER_DIRECTORY_EMAILADDRESS` should contain the `InternetAddress` value for that user

### TestFreeBusy

The `TestFreeBusy#testFreeTimeSearch` test case requires one environment property to run:

- `FREEBUSY_USERS` must contain a comma-separated list of Notes-format usernames to look up

### TestAgent

The `TestAgent#testRunOnServer` test case requires two environment properties to run:

- `ServerAgentDB` must be set to a Notes path to an accessible remote DB
- `ServerAgentName` must be set to the name of an agent in that database to run

### TestDatabase

The `TestDatabase#testLocalEncryption` test case looks for a database-path variable:

- `DATABASE_LOCALENC_PATH` should point to a database that is locally encrypted with 128-bit AES encryption

### TestServerInfo

The `TestServerInfo` tests look for server and admin info in two environemnt variables:

- `SERVER_INFO_SERVER` should contain the name of a server to query
- `SERVER_INFO_ADMINISTRATOR` should contain a name that is expected to exist in the Administrators field of that server's doc
