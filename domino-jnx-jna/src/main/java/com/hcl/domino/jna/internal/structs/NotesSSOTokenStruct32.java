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

import com.hcl.domino.commons.structs.ISSOTokenStruct;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Internal class to decode the SSO_TOKEN structure values
 * 
 * @author Karsten Lehmann
 */
public class NotesSSOTokenStruct32 extends BaseStructure implements ISSOTokenStruct {
	public int mhName;
	public int mhDomainList;
	public short wNumDomains;
	public int bSecureOnly;
	public int mhData;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSSOTokenStruct32() {
		super();
	}
	
	public static NotesSSOTokenStruct32 newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesSSOTokenStruct32>) () -> new NotesSSOTokenStruct32());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"mhName", //$NON-NLS-1$
			"mhDomainList", //$NON-NLS-1$
			"wNumDomains", //$NON-NLS-1$
			"bSecureOnly", //$NON-NLS-1$
			"mhData" //$NON-NLS-1$
		);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param mhName name for the token when set as a cookie
	 * @param mhDomainList list of DNS domains for the token when set as a cookie
	 * @param wNumDomains Total number of domains contained in the mhDomainList member
	 * @param bSecureOnly BOOL recommending that the token only be set on a secure connection.
	 * @param mhData MEMHANDLE to a the null-terminated token data.
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesSSOTokenStruct32(int mhName, int mhDomainList, short wNumDomains, int bSecureOnly, int mhData) {
		super();
		this.mhName = mhName;
		this.mhDomainList = mhDomainList;
		this.wNumDomains = wNumDomains;
		this.bSecureOnly = bSecureOnly;
		this.mhData = mhData;
	}
	
	public static NotesSSOTokenStruct32 newInstance(final int mhName, final int mhDomainList, final short wNumDomains, final int bSecureOnly, final int mhData) {
		return AccessController.doPrivileged((PrivilegedAction<NotesSSOTokenStruct32>) () -> new NotesSSOTokenStruct32(mhName, mhDomainList, wNumDomains, bSecureOnly, mhData));
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesSSOTokenStruct32(Pointer peer) {
		super(peer);
	}
	
	public static NotesSSOTokenStruct32 newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesSSOTokenStruct32>) () -> new NotesSSOTokenStruct32(p));
	}

	public boolean isSecureOnly() {
		return bSecureOnly!=0;
	}

	public static class ByReference extends NotesSSOTokenStruct32 implements Structure.ByReference {
		
	};
	
	public static class ByValue extends NotesSSOTokenStruct32 implements Structure.ByValue {
		
	}

	@Override
	public int getNameHandle() {
		return this.mhName;
	}

	@Override
	public int getDomainListHandle() {
		return this.mhDomainList;
	}

	@Override
	public short getNumDomains() {
		return this.wNumDomains;
	}

	@Override
	public int getSecureOnlyVal() {
		return this.bSecureOnly;
	}

	@Override
	public int getDataHandle() {
		return this.mhData;
	};
}
