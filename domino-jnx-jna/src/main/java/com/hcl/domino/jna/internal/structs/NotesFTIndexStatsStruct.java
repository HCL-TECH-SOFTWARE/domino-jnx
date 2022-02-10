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
 * JNA class for the FT_INDEX_STATS type
 * 
 * @author Karsten Lehmann
 */
public class NotesFTIndexStatsStruct extends BaseStructure {
	/** # of new documents */
	public int DocsAdded;
	/** # of revised documents */
	public int DocsUpdated;
	/** # of deleted documents */
	public int DocsDeleted;
	/** # of bytes indexed */
	public int BytesIndexed;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesFTIndexStatsStruct() {
		super();
	}
	
	public static NotesFTIndexStatsStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesFTIndexStatsStruct>) () -> new NotesFTIndexStatsStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"DocsAdded", //$NON-NLS-1$
			"DocsUpdated", //$NON-NLS-1$
			"DocsDeleted", //$NON-NLS-1$
			"BytesIndexed" //$NON-NLS-1$
		);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param DocsAdded # of new documents<br>
	 * @param DocsUpdated # of revised documents<br>
	 * @param DocsDeleted # of deleted documents<br>
	 * @param BytesIndexed # of bytes indexed
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesFTIndexStatsStruct(int DocsAdded, int DocsUpdated, int DocsDeleted, int BytesIndexed) {
		super();
		this.DocsAdded = DocsAdded;
		this.DocsUpdated = DocsUpdated;
		this.DocsDeleted = DocsDeleted;
		this.BytesIndexed = BytesIndexed;
	}
	
	public static NotesFTIndexStatsStruct newInstance(final int docsAdded, final int docsUpdated, final int docsDeleted, final int bytesIndexed) {
		return AccessController.doPrivileged((PrivilegedAction<NotesFTIndexStatsStruct>) () -> new NotesFTIndexStatsStruct(docsAdded, docsUpdated, docsDeleted, bytesIndexed));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * @param peer pointer
	 */
	@Deprecated
	public NotesFTIndexStatsStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesFTIndexStatsStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesFTIndexStatsStruct>) () -> new NotesFTIndexStatsStruct(peer));
	}
	
	public static class ByReference extends NotesFTIndexStatsStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesFTIndexStatsStruct implements Structure.ByValue {
		
	};
}
