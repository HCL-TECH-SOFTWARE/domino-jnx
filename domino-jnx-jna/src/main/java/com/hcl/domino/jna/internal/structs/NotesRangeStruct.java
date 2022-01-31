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
 * JNA class for the RANGE type
 * 
 * @author Karsten Lehmann
 */
public class NotesRangeStruct extends BaseStructure {
	/** list entries following */
	public short ListEntries;
	/** range entries following */
	public short RangeEntries;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesRangeStruct() {
		super();
	}
	
	public static NotesRangeStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesRangeStruct>) () -> new NotesRangeStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("ListEntries", "RangeEntries"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param ListEntries list entries following
	 * @param RangeEntries range entries following
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesRangeStruct(short ListEntries, short RangeEntries) {
		super();
		this.ListEntries = ListEntries;
		this.RangeEntries = RangeEntries;
	}
	
	public static NotesRangeStruct newInstance(final short ListEntries, final short RangeEntries) {
		return AccessController.doPrivileged((PrivilegedAction<NotesRangeStruct>) () -> new NotesRangeStruct(ListEntries, RangeEntries));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesRangeStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesRangeStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesRangeStruct>) () -> new NotesRangeStruct(p));
	}
	
	public static class ByReference extends NotesRangeStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesRangeStruct implements Structure.ByValue {
		
	};
}
