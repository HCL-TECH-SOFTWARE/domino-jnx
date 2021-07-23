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
 * JNA class for the RFC822ItemDesc type
 */
public class NotesRFC822ItemDescStruct extends BaseStructure {
	/** ODSSizeof this structure for versioning */
	public short wVersion;
	/** TYPE_822_TEXT flags.  The first three bits are reserved for the format mask,the remaining bits are flags */
	public int dwFlags;
	/** Length of the Notes version which is either a LMBCS string or a TIMEDATE */
	public short wNotesNativeLen;
	/** Length of the original 822 header name */
	public short w822NameLen;
	/** Length of the original 822 header delimiter */
	public short w822DelimLen;
	/** Length of the original 822 header body in its native charset and encoding (RFC2047) */
	public short w822BodyLen;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesRFC822ItemDescStruct() {
		super();
	}
	
	public static NotesRFC822ItemDescStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesRFC822ItemDescStruct>) () -> new NotesRFC822ItemDescStruct());
	}
	
	@SuppressWarnings("nls")
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("wVersion", "dwFlags", "wNotesNativeLen", "w822NameLen", "w822DelimLen", "w822BodyLen");
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesRFC822ItemDescStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesRFC822ItemDescStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesRFC822ItemDescStruct>) () -> new NotesRFC822ItemDescStruct(p));
	}
	
	public static class ByReference extends NotesRFC822ItemDescStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesRFC822ItemDescStruct implements Structure.ByValue {
		
	};
}
