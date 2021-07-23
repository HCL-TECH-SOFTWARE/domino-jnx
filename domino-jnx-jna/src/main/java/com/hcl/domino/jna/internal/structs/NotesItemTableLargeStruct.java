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
public class NotesItemTableLargeStruct extends Structure {
	/** total length of this buffer */
	public int Length;
	/** number of items in the table */
	public short Items;
	/** can't have padding for inplace ods -> host */
	public short filler;
	
	public NotesItemTableLargeStruct() {
		super();
	}
	
	public static NotesItemTableLargeStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemTableLargeStruct>) () -> new NotesItemTableLargeStruct());
	}

	protected List<String> getFieldOrder() {
		return Arrays.asList("Length", "Items", "filler");
	}
	
	/**
	 * @param Length total length of this buffer<br>
	 * @param Items number of items in the table<br>
	 * @param filler can't have padding for inplace ods -> host
	 */
	public NotesItemTableLargeStruct(int Length, short Items, short filler) {
		super();
		this.Length = Length;
		this.Items = Items;
		this.filler = filler;
	}
	
	public NotesItemTableLargeStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesItemTableLargeStruct newInstance(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemTableLargeStruct>) () -> new NotesItemTableLargeStruct(peer));
	}

	public static class ByReference extends NotesItemTableLargeStruct implements Structure.ByReference {
		
	};
	
	public static NotesItemTableLargeStruct.ByReference newInstanceByReference() {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemTableLargeStruct.ByReference>) () -> new NotesItemTableLargeStruct.ByReference());
	}

	public static class ByValue extends NotesItemTableLargeStruct implements Structure.ByValue {
		
	};
	
	public int getLengthAsInt() {
		return Length;
	}

	public int getItemsAsInt() {
		return Items  & 0xffff;
	}
}
