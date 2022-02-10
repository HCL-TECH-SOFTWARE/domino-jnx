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

import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This is the structure that identifies a replica database and stores the replication
 * options that affect how the Server's Replicator Task will manipulate the database.<br>
 * <br>
 * Some replication Flags, CutoffInterval, and Cutoff members correspond to the edit
 * controls in the the Workstation's Replication Settings dialog box (in the
 * File, Database, Properties InfoBox).<br>
 * <br>
 * The Replica ID is a {@link NotesTimeDateStruct} structure that contains the time/date
 * of the replica's creation, used to uniquely identify the database replicas
 * to each other.<br>
 * <br>
 * This time/date is NOT normalized to Greenwich Mean Time (GMT), as keeping the local
 * time zone and daylight savings time settings will further ensure that it is a unique time/date.
 */
public class NotesDbReplicaInfoStruct extends BaseStructure implements IAdaptable {
	/**
	 * ID that is same for all replica files<br>
	 * C type : TIMEDATE
	 */
	public NotesTimeDateStruct ID;
	/** Replication flags */
	public short Flags;
	/**
	 * Automatic Replication Cutoff<br>
	 * Interval (Days)
	 */
	public short CutoffInterval;
	/**
	 * Replication cutoff date<br>
	 * C type : TIMEDATE
	 */
	public NotesTimeDateStruct Cutoff;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesDbReplicaInfoStruct() {
		super();
	}
	
	public static NotesDbReplicaInfoStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesDbReplicaInfoStruct>) () -> new NotesDbReplicaInfoStruct());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesDbReplicaInfoStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	@SuppressWarnings("nls")
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("ID", "Flags", "CutoffInterval", "Cutoff");
	}
	
	/**
	 * @param ID ID that is same for all replica files<br>
	 * C type : TIMEDATE<br>
	 * @param Flags Replication flags<br>
	 * @param CutoffInterval Automatic Replication Cutoff<br>
	 * Interval (Days)<br>
	 * @param Cutoff Replication cutoff date<br>
	 * C type : TIMEDATE
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesDbReplicaInfoStruct(NotesTimeDateStruct ID, short Flags, short CutoffInterval, NotesTimeDateStruct Cutoff) {
		super();
		this.ID = ID;
		this.Flags = Flags;
		this.CutoffInterval = CutoffInterval;
		this.Cutoff = Cutoff;
	}
	
	public static NotesDbReplicaInfoStruct newInstance(final NotesTimeDateStruct ID, final short Flags, final short CutoffInterval, final NotesTimeDateStruct Cutoff) {
		return AccessController.doPrivileged((PrivilegedAction<NotesDbReplicaInfoStruct>) () -> new NotesDbReplicaInfoStruct(ID, Flags, CutoffInterval, Cutoff));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesDbReplicaInfoStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesDbReplicaInfoStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesDbReplicaInfoStruct>) () -> new NotesDbReplicaInfoStruct(peer));
	}
	
	public static class ByReference extends NotesDbReplicaInfoStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesDbReplicaInfoStruct implements Structure.ByValue {
		
	};
	
	/**
	 * Returns the replica ID ({@link #ID}) as hex encoded string with 16 characters
	 * 
	 * @return replica id
	 */
	public String getReplicaID() {
		return NotesStringUtils.innardsToReplicaId(this.ID.Innards);
	}
	
	/**
	 * Method to set the replica ID ({@link #ID}) as hex encoded string with 16 characters
	 * 
	 * @param replicaId new replica id, either 16 characters of 8:8 format
	 */
	public void setReplicaID(String replicaId) {
		if (replicaId.contains(":")) //$NON-NLS-1$
		 {
			replicaId = replicaId.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (replicaId.length() != 16) {
			throw new IllegalArgumentException("Replica ID is expected to have 16 characters");
		}
		
		this.ID.Innards[1] = Integer.parseInt(replicaId.substring(0,8), 16);
		this.ID.Innards[0] = Integer.parseInt(replicaId.substring(8), 16);
		write();
	}
}
