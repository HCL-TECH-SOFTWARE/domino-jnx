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
 * This is the schedule data. The {@link NotesScheduleListStruct} is followed by
 * NumEntries of {@link NotesSchedEntryStruct} or {@link NotesSchedEntryExtStruct}.
 * 
 * @author Karsten Lehmann
 */
public class NotesScheduleListStruct extends BaseStructure {
	/** Total number of schedule entries follow */
	public int NumEntries;
	/** Application id for UserAttr interpretation */
	public short wApplicationID;
	/**
	 * Pre Notes/Domino 6: spare <br>
	 * Notes/Domino 6: This now conveys the length of a single<br>
	 * SCHED_ENTRY_xxx that follows.  Use this value<br>
	 * to skip entries that MAY be larger (ie: a later version may<br>
	 * extend SCHED_ENTRY_EXT by appending values<br>
	 * that Notes/Domino 6 does not know about so SCHED_ENTRY_xxx<br>
	 * would actually be larger than the Notes/Domino 6<br>
	 * SCHED_ENTRY_EXT
	 */
	public short Spare;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesScheduleListStruct() {
		super();
	}
	
	public static NotesScheduleListStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesScheduleListStruct>) () -> new NotesScheduleListStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("NumEntries", "wApplicationID", "Spare"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @param NumEntries Total number of schedule entries follow<br>
	 * @param wApplicationID Application id for UserAttr interpretation<br>
	 * @param Spare Pre Notes/Domino 6: spare <br>
	 * Notes/Domino 6: This now conveys the length of a single<br>
	 * SCHED_ENTRY_xxx that follows.  Use this value<br>
	 * to skip entries that MAY be larger (ie: a later version may<br>
	 * extend SCHED_ENTRY_EXT by appending values<br>
	 * that Notes/Domino 6 does not know about so SCHED_ENTRY_xxx<br>
	 * would actually be larger than the Notes/Domino 6<br>
	 * SCHED_ENTRY_EXT
	 * 
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesScheduleListStruct(int NumEntries, short wApplicationID, short Spare) {
		super();
		this.NumEntries = NumEntries;
		this.wApplicationID = wApplicationID;
		this.Spare = Spare;
	}
	
	public static NotesScheduleListStruct newInstance(final int numEntries, final short wApplicationID, final short spare) {
		return AccessController.doPrivileged((PrivilegedAction<NotesScheduleListStruct>) () -> new NotesScheduleListStruct(numEntries, wApplicationID, spare));
	}
	
	public NotesScheduleListStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesScheduleListStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesScheduleListStruct>) () -> new NotesScheduleListStruct(peer));
	}
	
	public static class ByReference extends NotesScheduleListStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesScheduleListStruct implements Structure.ByValue {
		
	};
}
