# ID Vault Token Handlers

The `IDVault#getUserIdWithToken(Object token, String serverName)` method provides a generic way for an extension implementation to retrieve a `UserId` from an ID Vault in an implementation-specific way.

This can be done by implementing the `com.hcl.domino.admin.idvault.IdVaultTokenHandler` interface and registering it as a [`ServiceLoader`-compatible service](https://www.baeldung.com/java-spi). The implementation should check to see whether it can handle the provided token object (say, if it's a class from the implementation package) and then provide a mechanism to process it.

The `UserId` implementation returned by this service does not need to be specifically based on the JNA version of JNX. Beyond implementing the two main methods on the interface (though `#makeSafeCopy` could potentially be stubbed out depending on needs), it should provide a `getAdapter` implementation that can respond to a request for a JNA `PointerByReference`. For example, the `IdVault` class in the JNA implementation does this:

```java
PointerByReference rethKFC = new PointerByReference();
_getUserIdFromVault(userName, password, null, rethKFC, serverName);

//according to core dev, calling SECKFMClose is not required / not used in core platform code.
boolean noDispose = true;
return new JNAUserId(this, new IAdaptable() {
	
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if (PointerByReference.class == clazz) {
			return (T) rethKFC;
		} else if(Long.class.equals(clazz)) {
			return (T)Long.valueOf(Pointer.nativeValue(rethKFC.getPointer()));
		}

		return null;
	}
}, noDispose);
```

Where `rethKFC` is the `phKFC` argument from `SECidfGet`, since `KFHANDLE` is in turn just a pointer type.

The case of requesting a `Long` is good for future-proofing, in case we have another native implementation that doesn't use JNA specifically (or if the JNA implementation changes to use it for its own future-proofing).

Note: if referencing the `domino-jnx-jna` module as a dependency, implementations can use `NotesErrorUtils.checkResult(short)` to check standard Domino error codes and throw wrapped exceptions that use the text from `OSLoadString`.
