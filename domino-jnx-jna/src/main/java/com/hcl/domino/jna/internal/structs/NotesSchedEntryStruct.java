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

import com.hcl.domino.commons.structs.WrongArraySizeException;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This is the data structure that describes an individual schedule entry.
 */
public class NotesSchedEntryStruct extends BaseStructure {
	/** C type : UNID */
	public NotesUniversalNoteIdStruct Unid;
	/** C type : TIMEDATE_PAIR */
	public NotesTimeDatePairStruct Interval;
	public byte Attr;
	public byte UserAttr;
	/** C type : BYTE[2] */
	public byte[] spare = new byte[2];
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSchedEntryStruct() {
		super();
	}
	
	public static NotesSchedEntryStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesSchedEntryStruct>) () -> new NotesSchedEntryStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"Unid", //$NON-NLS-1$
			"Interval", //$NON-NLS-1$
			"Attr", //$NON-NLS-1$
			"UserAttr", //$NON-NLS-1$
			"spare" //$NON-NLS-1$
		);
	}
	
	/**
	 * @param Unid C type : UNID
	 * @param Interval C type : TIMEDATE_PAIR
	 * @param Attr attributes
	 * @param UserAttr user defined attributes
	 * @param spare unused BYTE[2]
	 * 
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSchedEntryStruct(NotesUniversalNoteIdStruct Unid, NotesTimeDatePairStruct Interval, byte Attr, byte UserAttr, byte spare[]) {
		super();
		this.Unid = Unid;
		this.Interval = Interval;
		this.Attr = Attr;
		this.UserAttr = UserAttr;
		if ((spare.length != this.spare.length)) {
			throw new WrongArraySizeException("spare"); //$NON-NLS-1$
		}
		this.spare = spare;
	}
	
	public static NotesSchedEntryStruct newInstance(final NotesUniversalNoteIdStruct Unid, final NotesTimeDatePairStruct Interval, final byte Attr, final byte UserAttr, final byte[] spare) {
		return AccessController.doPrivileged((PrivilegedAction<NotesSchedEntryStruct>) () -> new NotesSchedEntryStruct(Unid, Interval, Attr, UserAttr, spare));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesSchedEntryStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesSchedEntryStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesSchedEntryStruct>) () -> new NotesSchedEntryStruct(peer));
	}
	
	public static class ByReference extends NotesSchedEntryStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesSchedEntryStruct implements Structure.ByValue {
		
	};
}
