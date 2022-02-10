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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.commons.structs.WrongArraySizeException;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NotesViewColumnFormat5Struct extends BaseStructure {
	/** VIEW_COLUMN_FORMAT_SIGNATURE5 */
	public short Signature;
	/** sizeof this structure + any extra data. */
	public short dwLength;
	public int dwFlags;
	/** Length of programatic name of column that contains distiguished name. */
	public short wDistNameColLen;
	/** If shared column, length of the alias of the shared column */
	public short wSharedColumnAliasLen;
	/**
	 * Reserved for future use.<br>
	 * C type : DWORD[4]
	 */
	public int[] dwReserved = new int[4];
	public NotesViewColumnFormat5Struct() {
		super();
		setAlignType(Structure.ALIGN_NONE);
	}
	
	public static NotesViewColumnFormat5Struct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesViewColumnFormat5Struct>) () -> new NotesViewColumnFormat5Struct());
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"Signature", //$NON-NLS-1$
			"dwLength", //$NON-NLS-1$
			"dwFlags", //$NON-NLS-1$
			"wDistNameColLen", //$NON-NLS-1$
			"wSharedColumnAliasLen", //$NON-NLS-1$
			"dwReserved" //$NON-NLS-1$
		);
	}
	/**
	 * @param Signature VIEW_COLUMN_FORMAT_SIGNATURE5
	 * @param dwLength sizeof this structure + any extra data.
	 * @param dwFlags flags 
	 * @param wDistNameColLen Length of programatic name of column that contains distiguished name.
	 * @param wSharedColumnAliasLen If shared column, length of the alias of the shared column
	 * @param dwReserved Reserved for future use.
	 * C type : DWORD[4]
	 */
	public NotesViewColumnFormat5Struct(short Signature, short dwLength, int dwFlags, short wDistNameColLen, short wSharedColumnAliasLen, int dwReserved[]) {
		super();
		this.Signature = Signature;
		this.dwLength = dwLength;
		this.dwFlags = dwFlags;
		this.wDistNameColLen = wDistNameColLen;
		this.wSharedColumnAliasLen = wSharedColumnAliasLen;
		if ((dwReserved.length != this.dwReserved.length)) {
			throw new WrongArraySizeException("dwReserved"); //$NON-NLS-1$
		}
		this.dwReserved = dwReserved;
		setAlignType(Structure.ALIGN_NONE);
	}
	public NotesViewColumnFormat5Struct(Pointer peer) {
		super(peer);
		setAlignType(Structure.ALIGN_NONE);
	}
	
	public static NotesViewColumnFormat5Struct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesViewColumnFormat5Struct>) () -> new NotesViewColumnFormat5Struct(peer));
	}

	public static class ByReference extends NotesViewColumnFormat5Struct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesViewColumnFormat5Struct implements Structure.ByValue {
		
	};
}
