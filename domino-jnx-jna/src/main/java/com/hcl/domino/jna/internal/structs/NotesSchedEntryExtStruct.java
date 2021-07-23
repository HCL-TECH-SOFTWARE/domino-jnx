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
 * This is the extended data structure that describes an individual schedule entry.
 */
public class NotesSchedEntryExtStruct extends BaseStructure {
	/** C type : UNID */
	public NotesUniversalNoteIdStruct Unid;
	/** C type : TIMEDATE_PAIR */
	public NotesTimeDatePairStruct Interval;
	public byte Attr;
	public byte UserAttr;
	/** C type : BYTE[2] */
	public byte[] spare = new byte[2];

	/* Everything above this point is the same as NotesSchedEntryStruct for preR6 clients!
	 * Everything from here on down is R6 (or later) only! */

	public NotesUniversalNoteIdStruct ApptUnid;   /* ApptUNID of the entry */
	public int dwEntrySize; /* Size of this entry (for future ease of expansion) */
	public double nLongitude;     /* Longitude coordinate value */
	public double nLatitude;      /* Latitude coordinate value */
	    
	/**
	 * Creates a new entry
	 * 
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSchedEntryExtStruct() {
		super();
	}
	
	public static NotesSchedEntryExtStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesSchedEntryExtStruct>) () -> new NotesSchedEntryExtStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"Unid", //$NON-NLS-1$
			"Interval", //$NON-NLS-1$
			"Attr", //$NON-NLS-1$
			"UserAttr", //$NON-NLS-1$
			"spare", //$NON-NLS-1$
			"ApptUnid", //$NON-NLS-1$
			"dwEntrySize", //$NON-NLS-1$
			"nLongitude", //$NON-NLS-1$
			"nLatitude" //$NON-NLS-1$
		);
	}
	
	/**
	 * Creates a new entry
	 * 
	 * @param Unid C type : UNID<br>
	 * @param Interval C type : TIMEDATE_PAIR<br>
	 * @param Attr SCHED_ATTR_xxx attributes defined by Notes
	 * @param UserAttr Application specific attributes
	 * @param spare C type : BYTE[2]
	 * @param ApptUnid ApptUNID of the appointment note
	 * @param dwEntrySize Size of this entry (for future ease of expansion)
	 * @param nLongitude Geographical coordinates of the entry: longitude
	 * @param nLatitude Geographical coordinates of the entry: latitude
	 * 
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSchedEntryExtStruct(NotesUniversalNoteIdStruct Unid, NotesTimeDatePairStruct Interval,
			byte Attr, byte UserAttr, byte spare[],
			NotesUniversalNoteIdStruct ApptUnid, int dwEntrySize, double nLongitude, double nLatitude) {
		super();
		this.Unid = Unid;
		this.Interval = Interval;
		this.Attr = Attr;
		this.UserAttr = UserAttr;
		if ((spare.length != this.spare.length)) {
			throw new WrongArraySizeException("spare"); //$NON-NLS-1$
		}
		this.spare = spare;
		this.ApptUnid = ApptUnid;
		this.dwEntrySize = dwEntrySize;
		this.nLongitude = nLongitude;
		this.nLatitude = nLatitude;
	}
	
	public static NotesSchedEntryExtStruct newInstance(final NotesUniversalNoteIdStruct Unid,
			final NotesTimeDatePairStruct Interval, final byte Attr, final byte UserAttr, final byte[] spare,
			final NotesUniversalNoteIdStruct ApptUnid, final int dwEntrySize, final double nLongitude, final double nLatitude) {
		return AccessController.doPrivileged((PrivilegedAction<NotesSchedEntryExtStruct>) () -> new NotesSchedEntryExtStruct(Unid, Interval, Attr, UserAttr, spare, ApptUnid,
				dwEntrySize, nLongitude, nLatitude));
	}
	
	/**
	 * Creates a new entry
	 * 
	 * @param peer pointer
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSchedEntryExtStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesSchedEntryExtStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesSchedEntryExtStruct>) () -> new NotesSchedEntryExtStruct(peer));
	}
	
	public static class ByReference extends NotesSchedEntryExtStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesSchedEntryExtStruct implements Structure.ByValue {
		
	};
}
