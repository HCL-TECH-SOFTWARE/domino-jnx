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
 * JNA class for the ITEM_VALUE_TABLE type
 * 
 * @author Karsten Lehmann
 */
public class NotesItemValueTableStruct extends BaseStructure {
	/** total length of this buffer */
	public short Length;
	/** number of items in the table */
	public short Items;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesItemValueTableStruct() {
		super();
	}
	
	public static NotesItemValueTableStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemValueTableStruct>) () -> new NotesItemValueTableStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Length", "Items"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param length total length of this buffer<br>
	 * @param items number of items in the table
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesItemValueTableStruct(short length, short items) {
		super();
		this.Length = length;
		this.Items = items;
	}
	
	public static NotesItemValueTableStruct newInstance(final short length, final short items) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemValueTableStruct>) () -> new NotesItemValueTableStruct(length, items));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesItemValueTableStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesItemValueTableStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemValueTableStruct>) () -> new NotesItemValueTableStruct(peer));
	}

	public static class ByReference extends NotesItemValueTableStruct implements Structure.ByReference {
		
	};
	
	public static class ByValue extends NotesItemValueTableStruct implements Structure.ByValue {
		
	};
	
	public int getLengthAsInt() {
		return Length  & 0xffff;
	}

	public int getItemsAsInt() {
		return Items  & 0xffff;
	}

}
