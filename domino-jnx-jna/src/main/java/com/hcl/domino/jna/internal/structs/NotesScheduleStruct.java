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

/**
 * Data structure for a schedule.
 */
public class NotesScheduleStruct extends BaseStructure {
	/** C type : DWORD[8] */
	public int[] reserved = new int[8];
	/**
	 * Users mail file replica ID<br>
	 * C type : DBID
	 */
	public NotesTimeDateStruct dbReplicaID;
	/**
	 * events etc. are in this<br>
	 * interval<br>
	 * C type : TIMEDATE_PAIR
	 */
	public NotesTimeDatePairStruct Interval;
	/**
	 * gateway error retrieving this<br>
	 * schedule
	 */
	public int dwErrGateway;
	/**
	 * error retrieving this<br>
	 * schedule<br>
	 * C type : STATUS
	 */
	public short error;
	/** unused at this time */
	public short wReserved;
	/**
	 * size of owner name<br>
	 * (includes term.)
	 */
	public short wOwnerNameSize;

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesScheduleStruct() {
		super();
	}
	
	public static NotesScheduleStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesScheduleStruct>) () -> new NotesScheduleStruct());
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"reserved", //$NON-NLS-1$
			"dbReplicaID", //$NON-NLS-1$
			"Interval", //$NON-NLS-1$
			"dwErrGateway", //$NON-NLS-1$
			"error", //$NON-NLS-1$
			"wReserved", //$NON-NLS-1$
			"wOwnerNameSize" //$NON-NLS-1$
		);
	}
	/**
	 * @param reserved C type : DWORD[8]<br>
	 * @param dbReplicaID Users mail file replica ID<br>
	 * C type : DBID<br>
	 * @param Interval events etc. are in this<br>
	 * interval<br>
	 * C type : TIMEDATE_PAIR<br>
	 * @param dwErrGateway gateway error retrieving this<br>
	 * schedule<br>
	 * @param error error retrieving this<br>
	 * schedule<br>
	 * C type : STATUS<br>
	 * @param wReserved unused at this time<br>
	 * @param wOwnerNameSize size of owner name<br>
	 * (includes term.)
	 * 
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesScheduleStruct(int reserved[], NotesTimeDateStruct dbReplicaID, NotesTimeDatePairStruct Interval, int dwErrGateway, short error,
			short wReserved, short wOwnerNameSize) {
		super();
		if ((reserved.length != this.reserved.length)) {
			throw new WrongArraySizeException("reserved"); //$NON-NLS-1$
		}
		this.reserved = reserved;
		this.dbReplicaID = dbReplicaID;
		this.Interval = Interval;
		this.dwErrGateway = dwErrGateway;
		this.error = error;
		this.wReserved = wReserved;
		this.wOwnerNameSize = wOwnerNameSize;
	}
	
	public static NotesScheduleStruct newInstance(final int reserved[], final NotesTimeDateStruct dbReplicaID, final NotesTimeDatePairStruct interval, final int dwErrGateway, final short error,
			final short wReserved, final short wOwnerNameSize) {
		return AccessController.doPrivileged((PrivilegedAction<NotesScheduleStruct>) () -> new NotesScheduleStruct(reserved, dbReplicaID, interval, dwErrGateway, error,
				wReserved, wOwnerNameSize));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesScheduleStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesScheduleStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesScheduleStruct>) () -> new NotesScheduleStruct(peer));
	}
	
	public static class ByReference extends NotesScheduleStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesScheduleStruct implements Structure.ByValue {
		
	}
}
