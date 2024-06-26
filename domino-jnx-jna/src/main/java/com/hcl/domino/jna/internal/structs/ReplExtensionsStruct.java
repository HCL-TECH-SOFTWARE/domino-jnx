/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jna.internal.structs;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ReplExtensionsStruct extends BaseStructure {
	/**
	 * sizeof(REPLEXTENSIONS), allows for future<br>
	 * expansion
	 */
	public short Size;
	/**
	 * If non-zero, number of minutes replication<br>
	 * is allowed to execute before cancellation.<br>
	 * If not specified, no limit is imposed
	 */
	public short TimeLimit;
	
	public ReplExtensionsStruct() {
		super();
	}
	
	public static ReplExtensionsStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<ReplExtensionsStruct>) () -> new ReplExtensionsStruct());
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Size", "TimeLimit"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param Size sizeof(REPLEXTENSIONS), allows for future<br>
	 * expansion<br>
	 * @param TimeLimit If non-zero, number of minutes replication<br>
	 * is allowed to execute before cancellation.<br>
	 * If not specified, no limit is imposed
	 */
	public ReplExtensionsStruct(short Size, short TimeLimit) {
		super();
		this.Size = Size;
		this.TimeLimit = TimeLimit;
	}
	
	public static ReplExtensionsStruct newInstance(final short Size, final short TimeLimit) {
		return AccessController.doPrivileged((PrivilegedAction<ReplExtensionsStruct>) () -> new ReplExtensionsStruct(Size, TimeLimit));
	}

	public ReplExtensionsStruct(Pointer peer) {
		super(peer);
	}

	public static ReplExtensionsStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ReplExtensionsStruct>) () -> new ReplExtensionsStruct(peer));
	}

	public static class ByReference extends ReplExtensionsStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends ReplExtensionsStruct implements Structure.ByValue {
		
	};
}
