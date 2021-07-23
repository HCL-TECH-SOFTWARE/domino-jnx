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
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
/**
 * This structure is returned by ReplicateWithServer() and ReplicateWithServerExt().<br>
 * It contains the resulting replication statistics information.
 */
public class ReplFileStatsStruct extends BaseStructure {
	public NativeLong TotalFiles;
	public NativeLong FilesCompleted;
	public NativeLong NotesAdded;
	public NativeLong NotesDeleted;
	public NativeLong NotesUpdated;
	public NativeLong Successful;
	public NativeLong Failed;
	public NativeLong NumberErrors;
	
	public ReplFileStatsStruct() {
		super();
	}
	
	public static ReplFileStatsStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<ReplFileStatsStruct>) () -> new ReplFileStatsStruct());
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"TotalFiles", //$NON-NLS-1$
			"FilesCompleted", //$NON-NLS-1$
			"NotesAdded", //$NON-NLS-1$
			"NotesDeleted", //$NON-NLS-1$
			"NotesUpdated", //$NON-NLS-1$
			"Successful", //$NON-NLS-1$
			"Failed", //$NON-NLS-1$
			"NumberErrors" //$NON-NLS-1$
		);
	}
	
	public ReplFileStatsStruct(NativeLong TotalFiles, NativeLong FilesCompleted, NativeLong NotesAdded, NativeLong NotesDeleted, NativeLong NotesUpdated, NativeLong Successful, NativeLong Failed, NativeLong NumberErrors) {
		super();
		this.TotalFiles = TotalFiles;
		this.FilesCompleted = FilesCompleted;
		this.NotesAdded = NotesAdded;
		this.NotesDeleted = NotesDeleted;
		this.NotesUpdated = NotesUpdated;
		this.Successful = Successful;
		this.Failed = Failed;
		this.NumberErrors = NumberErrors;
	}

	public static ReplFileStatsStruct newInstance(final NativeLong TotalFiles, final NativeLong FilesCompleted, final NativeLong NotesAdded, final NativeLong NotesDeleted, final NativeLong NotesUpdated, final NativeLong Successful, final NativeLong Failed, final NativeLong NumberErrors) {
		return AccessController.doPrivileged((PrivilegedAction<ReplFileStatsStruct>) () -> new ReplFileStatsStruct(TotalFiles, FilesCompleted, NotesAdded, NotesDeleted, NotesUpdated, Successful, Failed, NumberErrors));
	}

	public ReplFileStatsStruct(Pointer peer) {
		super(peer);
	}
	
	public static ReplFileStatsStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ReplFileStatsStruct>) () -> new ReplFileStatsStruct(peer));
	}
	
	public static class ByReference extends ReplFileStatsStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends ReplFileStatsStruct implements Structure.ByValue {
		
	};
}
