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

import com.hcl.domino.data.IAdaptable;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
/**
 * This structure is returned from ReplicateWithServer and ReplicateWithServerExt.<br>
 * It contains the returned replication statistics.
 */
public class ReplServStatsStruct extends BaseStructure implements IAdaptable {
	/** C type : REPLFILESTATS */
	public ReplFileStatsStruct Pull;
	/** C type : REPLFILESTATS */
	public ReplFileStatsStruct Push;
	public NativeLong StubsInitialized;
	public NativeLong TotalUnreadExchanges;
	public NativeLong NumberErrors;
	public short LastError;
	
	public ReplServStatsStruct() {
		super();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == ReplServStatsStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static ReplServStatsStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<ReplServStatsStruct>) () -> new ReplServStatsStruct());
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getFieldOrder() {
		return Arrays.asList("Pull", "Push", "StubsInitialized", "TotalUnreadExchanges", "NumberErrors", "LastError");
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param Pull pull statistics
	 * @param Push push statistics
	 * @param StubsInitialized number of stubs initialized
	 * @param TotalUnreadExchanges number of total unread exchanges
	 * @param NumberErrors number of errors
	 * @param LastError last error code
	 */
	public ReplServStatsStruct(ReplFileStatsStruct Pull, ReplFileStatsStruct Push, NativeLong StubsInitialized, NativeLong TotalUnreadExchanges, NativeLong NumberErrors, short LastError) {
		super();
		this.Pull = Pull;
		this.Push = Push;
		this.StubsInitialized = StubsInitialized;
		this.TotalUnreadExchanges = TotalUnreadExchanges;
		this.NumberErrors = NumberErrors;
		this.LastError = LastError;
	}
	
	public static ReplServStatsStruct newInstance(final ReplFileStatsStruct Pull, final ReplFileStatsStruct Push, final NativeLong StubsInitialized, final NativeLong TotalUnreadExchanges, final NativeLong NumberErrors, final short LastError) {
		return AccessController.doPrivileged((PrivilegedAction<ReplServStatsStruct>) () -> new ReplServStatsStruct(Pull, Push, StubsInitialized, TotalUnreadExchanges, NumberErrors, LastError));
	}

	public ReplServStatsStruct(Pointer peer) {
		super(peer);
	}
	
	public static ReplServStatsStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ReplServStatsStruct>) () -> new ReplServStatsStruct(peer));
	}

	public static class ByReference extends ReplServStatsStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends ReplServStatsStruct implements Structure.ByValue {
		
	};
}
