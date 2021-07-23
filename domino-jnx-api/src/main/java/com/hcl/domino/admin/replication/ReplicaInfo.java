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
package com.hcl.domino.admin.replication;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;

public interface ReplicaInfo extends IAdaptable {

	/**
	 * Returns the database this replica info belongs to
	 * 
	 * @return parent database
	 */
	Database getParentDatabase();
	
	/**
	 * Returns the replication ID which is same for all replica files
	 * 
	 * @return ID
	 */
	DominoDateTime getReplicaIDAsDate();
	
	/**
	 * Sets the replication ID which is same for all replica files
	 * 
	 * @param newID new ID
	 */
	void setReplicaIDAsDate(TemporalAccessor newID);
	
	/**
	 * Checks if the database design is hidden
	 * 
	 * @return true if hidden
	 */
	boolean isDesignHidden();
	
	/**
	 * Automatic Replication Cutoff Interval (Days)
	 * 
	 * @return interval
	 */
	int getCutOffInterval();
	
	/**
	 * Sets the Automatic Replication Cutoff Interval (Days)
	 * 
	 * @param interval (WORD)
	 */
	void setCutOffInterval(int interval);
	
	/**
	 * Replication cutoff date
	 * 
	 * @return an {@link Optional} describing the cutoff date if set, or an empty one otherwise
	 */
	Optional<DominoDateTime> getCutOff();
	
	/**
	 * Sets the new cutoff date
	 * 
	 * @param cutOff date
	 */
	void setCutOff(TemporalAccessor cutOff);

	/**
	 * Returns the replica ID as hex encoded string with 16 characters
	 * 
	 * @return replica id
	 */
	String getReplicaID();
	
	/**
	 * Method to set the replica ID as hex encoded string with 16 characters
	 * 
	 * @param replicaId new replica id, either 16 characters of 8:8 format
	 */
	void setReplicaID(String replicaId);
	
	/**
	 * Computes a new replica id and calls {@link #setReplicaID(String)}
	 * 
	 * @return the new replica id
	 */
	String setNewReplicaId();

	Set<ReplicationFlags> getReplicationFlags();
	
	/**
	 * Returns true if a replication flag is set
	 * 
	 * @param flag flag
	 * @return true if set
	 */
	boolean isReplicationFlagSet(ReplicationFlags flag);
	
	/**
	 * Method to change a replication flag
	 * 
	 * @param flag flag to change
	 * @param on new flag value (on or off)
	 */
	void setReplicationFlag(ReplicationFlags flag, boolean on);
	
	public enum Priority {LOW, MEDIUM, HIGH}
	
	/**
	 * Reads the current replication priority
	 * 
	 * @return priority
	 */
	Priority getPriority();
	
	/**
	 * Changes the replication priority
	 * 
	 * @param priority new priority
	 */
	void setPriority(Priority priority);
	
}
