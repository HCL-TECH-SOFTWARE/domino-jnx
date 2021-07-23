/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesBlockIdStruct extends BaseStructure {
	/**
	 * pool handle<br>
	 * C type : DHANDLE
	 */
	public int pool;
	/**
	 * block handle<br>
	 * C type : BLOCK
	 */
	public short block;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesBlockIdStruct() {
		super();
//		setAlignType(Structure.ALIGN_NONE);
		
	}
	
	public static NotesBlockIdStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesBlockIdStruct>) () -> new NotesBlockIdStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("pool", "block"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param pool pool handle<br>
	 * C type : DHANDLE<br>
	 * @param block block handle<br>
	 * C type : BLOCK
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesBlockIdStruct(int pool, short block) {
		super();
//		setAlignType(Structure.ALIGN_NONE);
		this.pool = pool;
		this.block = block;
	}
	
	public static NotesBlockIdStruct newInstance(final int pool, final short block) {
		return AccessController.doPrivileged((PrivilegedAction<NotesBlockIdStruct>) () -> new NotesBlockIdStruct(pool, block));
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesBlockIdStruct(Pointer peer) {
		super(peer);
//		setAlignType(Structure.ALIGN_NONE);
	}
	
	public static NotesBlockIdStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesBlockIdStruct>) () -> new NotesBlockIdStruct(p));
	}
	
	public static class ByReference extends NotesBlockIdStruct implements Structure.ByReference {
		public static NotesBlockIdStruct.ByReference newInstance() {
			return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> new NotesBlockIdStruct.ByReference());
		}
	};
	public static class ByValue extends NotesBlockIdStruct implements Structure.ByValue {
		public static NotesBlockIdStruct.ByValue newInstance() {
			return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> new NotesBlockIdStruct.ByValue());
		}
	};
}
