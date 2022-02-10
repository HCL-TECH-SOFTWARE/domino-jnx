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
 * If requested, this structure is returned by {@code JNADominoCollection#readEntries(com.mindoo.domino.jna.NotesCollectionPosition, java.util.EnumSet, int, java.util.EnumSet, int, java.util.EnumSet)}
 * at the front of the returned information buffer.<br>
 * The structure describes statistics about the overall collection.
 * 
 * @author Karsten Lehmann
 */
public class NotesCollectionStatsStruct extends BaseStructure {
	/** # top level entries (level 0) */
	public int TopLevelEntries;
	/** 0 */
	public int LastModifiedTime;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesCollectionStatsStruct() {
		super();
	}
	
	public static NotesCollectionStatsStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollectionStatsStruct>) () -> new NotesCollectionStatsStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("TopLevelEntries", "LastModifiedTime"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param TopLevelEntries # top level entries (level 0)<br>
	 * @param LastModifiedTime 0
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesCollectionStatsStruct(int TopLevelEntries, int LastModifiedTime) {
		super();
		this.TopLevelEntries = TopLevelEntries;
		this.LastModifiedTime = LastModifiedTime;
	}
	
	public static NotesCollectionStatsStruct newInstance(final int TopLevelEntries, final int LastModifiedTime) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollectionStatsStruct>) () -> new NotesCollectionStatsStruct(TopLevelEntries, LastModifiedTime));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesCollectionStatsStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesCollectionStatsStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollectionStatsStruct>) () -> new NotesCollectionStatsStruct(peer));
	}
	
	public static class ByReference extends NotesCollectionStatsStruct implements Structure.ByReference {
		
	};
	
	public static class ByValue extends NotesCollectionStatsStruct implements Structure.ByValue {
		
	};
}
