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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA class for the ITEM type
 * 
 * @author Karsten Lehmann
 */
public class NotesTableItemStruct extends BaseStructure {
	/** Length of Item Name following this struct. may be zero (0) if not required by func(s)*/
	public short NameLength;
	/** Length of Item Value following this struct, incl. Notes data type.        */
	public short ValueLength;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTableItemStruct() {
		super();
	}
	
	public static NotesTableItemStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesTableItemStruct>) () -> new NotesTableItemStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("NameLength", "ValueLength"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param NameLength length of item name
	 * @param ValueLength length of item value
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTableItemStruct(short NameLength, short ValueLength) {
		super();
		this.NameLength = NameLength;
		this.ValueLength = ValueLength;
	}
	
	public static NotesTableItemStruct newInstance(final short NameLength, final short ValueLength) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTableItemStruct>) () -> new NotesTableItemStruct(NameLength, ValueLength));
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesTableItemStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesTableItemStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTableItemStruct>) () -> new NotesTableItemStruct(p));
	}
	
	public static class ByReference extends NotesTableItemStruct implements Structure.ByReference {
		
	};
	
	public static class ByValue extends NotesTableItemStruct implements Structure.ByValue {
		
	};
	
	public int getNameLengthAsInt() {
		return NameLength & 0xffff;
	}

	public int getValueLengthAsInt() {
		return ValueLength & 0xffff;
	}

}
