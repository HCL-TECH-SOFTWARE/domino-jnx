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
package com.hcl.domino.jna.admin.replication;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.admin.replication.ReplicaInfo;
import com.hcl.domino.admin.replication.ReplicationFlags;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.structs.NotesDbReplicaInfoStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
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
public class JNAReplicaInfo implements IAdaptable, ReplicaInfo {
	private Database m_database;
	private NotesDbReplicaInfoStruct m_struct;
	
	/**
	 * Creates a new instance
	 * 
	 * @param db the parent database object
	 * @param adaptable object providing a supported data object for the time/date state
	 */
	public JNAReplicaInfo(JNADatabase db, IAdaptable adaptable) {
		m_database = db;
		
		NotesDbReplicaInfoStruct struct = adaptable.getAdapter(NotesDbReplicaInfoStruct.class);
		if (struct!=null) {
			m_struct = struct;
			return;
		}
		Pointer p = adaptable.getAdapter(Pointer.class);
		if (p!=null) {
			m_struct = NotesDbReplicaInfoStruct.newInstance(p);
			return;
		}
		throw new IllegalArgumentException("Constructor argument cannot provide a supported datatype");
	}
	
	public JNAReplicaInfo(JNADatabase db) {
		this(db, NotesDbReplicaInfoStruct.newInstance());
	}
	
	@Override
	public Database getParentDatabase() {
		return m_database;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesDbReplicaInfoStruct.class || clazz == Structure.class) {
			return (T) m_struct;
		}
		return null;
	}

	@Override
	public DominoDateTime getReplicaIDAsDate() {
		NotesTimeDateStruct id = m_struct.ID;
		if(id == null) {
			throw new IllegalStateException("Replica info ID is null");
		}
		return new JNADominoDateTime(id);
	}

	@Override
	public void setReplicaIDAsDate(TemporalAccessor newID) {
		m_struct.ID = NotesTimeDateStruct.newInstance(newID);
		m_struct.write();
	}

	@Override
	public boolean isDesignHidden() {
		return (m_struct.Flags & 0x0020) == 0x0020;
	}
	
	@Override
	public int getCutOffInterval() {
		return m_struct.CutoffInterval & 0xffff;
	}

	@Override
	public void setCutOffInterval(int interval) {
		m_struct.CutoffInterval = (short) (interval & 0xffff);
		m_struct.write();
	}
	
	@Override
	public Optional<DominoDateTime> getCutOff() {
		if (m_struct.Cutoff==null || (m_struct.Cutoff.Innards[0]==0 && m_struct.Cutoff.Innards[1]==0)) {
			return Optional.empty();
		}
		return Optional.of(new JNADominoDateTime(m_struct.Cutoff));
	}
	
	@Override
	public void setCutOff(TemporalAccessor cutOff) {
		m_struct.Cutoff = cutOff==null ? null : NotesTimeDateStruct.newInstance(cutOff);
		m_struct.write();
	}
	
	@Override
	public String getReplicaID() {
		return NotesStringUtils.innardsToReplicaId(m_struct.ID.Innards);
	}
	
	@Override
	public void setReplicaID(String replicaId) {
		if (replicaId.contains(":")) //$NON-NLS-1$
		 {
			replicaId = replicaId.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (replicaId.length() != 16) {
			throw new IllegalArgumentException("Replica ID is expected to have 16 characters");
		}
		
		m_struct.ID.Innards[1] = Integer.parseInt(replicaId.substring(0,8), 16);
		m_struct.ID.Innards[0] = Integer.parseInt(replicaId.substring(8), 16);
		m_struct.write();
	}
	
	@Override
	public String setNewReplicaId() {
		setReplicaIDAsDate(Instant.now());
		return getReplicaID();
	}

	@Override
	public Set<ReplicationFlags> getReplicationFlags() {
		return DominoEnumUtil.valuesOf(ReplicationFlags.class, m_struct.Flags);
	}

	@Override
	public boolean isReplicationFlagSet(ReplicationFlags flag) {
		return (m_struct.Flags & flag.getValue()) == flag.getValue();
	}

	@Override
	public void setReplicationFlag(ReplicationFlags flag, boolean on) {
		if (isReplicationFlagSet(flag)) {
			if (on) {
				return;
			}
			
			m_struct.Flags -= flag.getValue();
		}
		else {
			if (!on) {
				return;
			}
			
			m_struct.Flags |= flag.getValue();
		}
		m_struct.write();
	}


	@Override
	public Priority getPriority() {
		if ((m_struct.Flags & NotesConstants.REPLFLG_PRIORITY_LOW) == NotesConstants.REPLFLG_PRIORITY_LOW) {
			return Priority.LOW;
		}
		if ((m_struct.Flags & NotesConstants.REPLFLG_PRIORITY_HI) == NotesConstants.REPLFLG_PRIORITY_HI) {
			return Priority.HIGH;
		}
		return Priority.MEDIUM;
	}

	@Override
	public void setPriority(Priority priority) {
		switch (priority) {
		case MEDIUM:
			m_struct.Flags = (short) ((m_struct.Flags & ~NotesConstants.REPLFLG_PRIORITY_HI) & 0xffff);
			m_struct.Flags = (short) ((m_struct.Flags & ~NotesConstants.REPLFLG_PRIORITY_LOW) & 0xffff);
			break;
		case HIGH:
			m_struct.Flags = (short) ((m_struct.Flags & ~NotesConstants.REPLFLG_PRIORITY_LOW) & 0xffff);
			m_struct.Flags |= NotesConstants.REPLFLG_PRIORITY_HI;
			break;
		case LOW:
		default:
			m_struct.Flags = (short) ((m_struct.Flags & ~NotesConstants.REPLFLG_PRIORITY_HI) & 0xffff);
			m_struct.Flags |= NotesConstants.REPLFLG_PRIORITY_LOW;
			break;
		}
		m_struct.write();
	}

	@Override
	public String toString() {
		return MessageFormat.format(
			"JNAReplicaInfo [database={0}, replicaid={1}, cutoffdate={2}, getoffinterval={3}, priority={4}, flags={5}]", //$NON-NLS-1$
			m_database, getReplicaID(), getCutOff(), getCutOffInterval(), getPriority(), getReplicationFlags()
		);
	}
	
	
}
