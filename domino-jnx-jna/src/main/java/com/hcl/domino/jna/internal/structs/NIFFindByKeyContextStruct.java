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

public class NIFFindByKeyContextStruct extends BaseStructure {
	public short EntriesThisChunk;
	public short wSizeOfChunk;
	/** C type : void* */
	public Pointer SummaryBuffer;
	public int hUserData;
	public int UserDataLen;
	public int TotalDataInBuffer;

	public NIFFindByKeyContextStruct() {
		super();
		setAlignType(Structure.ALIGN_DEFAULT);
	}

	public static NIFFindByKeyContextStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NIFFindByKeyContextStruct>) () -> new NIFFindByKeyContextStruct());
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getFieldOrder() {
		return Arrays.asList("EntriesThisChunk", "wSizeOfChunk", "SummaryBuffer", "hUserData", "UserDataLen", "TotalDataInBuffer");
	}

	/**
	 * Creates a new context
	 * 
	 * @param EntriesThisChunk entries in this chunk
	 * @param wSizeOfChunk size of chunk
	 * @param SummaryBuffer summary buffer pointer
	 * @param hUserData handle user data
	 * @param UserDataLen length of user data
	 * @param TotalDataInBuffer total size of buffer
	 */
	public NIFFindByKeyContextStruct(short EntriesThisChunk, short wSizeOfChunk, Pointer SummaryBuffer, int hUserData, int UserDataLen, int TotalDataInBuffer) {
		super();
		this.EntriesThisChunk = EntriesThisChunk;
		this.wSizeOfChunk = wSizeOfChunk;
		this.SummaryBuffer = SummaryBuffer;
		this.hUserData = hUserData;
		this.UserDataLen = UserDataLen;
		this.TotalDataInBuffer = TotalDataInBuffer;
		setAlignType(Structure.ALIGN_DEFAULT);
	}

	public static NIFFindByKeyContextStruct newInstance(final short EntriesThisChunk, final short wSizeOfChunk, final Pointer SummaryBuffer, final int hUserData, final int UserDataLen, final int TotalDataInBuffer) {
		return AccessController.doPrivileged((PrivilegedAction<NIFFindByKeyContextStruct>) () -> new NIFFindByKeyContextStruct(EntriesThisChunk, wSizeOfChunk, SummaryBuffer, hUserData, UserDataLen, TotalDataInBuffer));
	}

	public NIFFindByKeyContextStruct(Pointer peer) {
		super(peer);
		setAlignType(Structure.ALIGN_DEFAULT);
	}

	public static NIFFindByKeyContextStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NIFFindByKeyContextStruct>) () -> new NIFFindByKeyContextStruct(peer));	
	}

	public static class ByReference extends NIFFindByKeyContextStruct implements Structure.ByReference {

	};

	public static class ByValue extends NIFFindByKeyContextStruct implements Structure.ByValue {

	};
}
