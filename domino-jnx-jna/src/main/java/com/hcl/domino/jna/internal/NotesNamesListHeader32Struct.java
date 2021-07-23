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
package com.hcl.domino.jna.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.commons.structs.WrongArraySizeException;
import com.hcl.domino.jna.internal.structs.BaseStructure;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA class to the NAMES_LIST type on Linux 32 bit platforms
 * 
 * @author Karsten Lehmann
 */
public class NotesNamesListHeader32Struct extends BaseStructure {
	/** Number of names in list */
	public short NumNames;

	/**
	 * User's license - now obsolete<br>
	 * C type : LICENSEID
	 */
	
	/**
	 * license number<br>
	 * C type : BYTE[5]
	 */
	public byte[] ID = new byte[5];
	/** product code, mfgr-specific */
	public byte Product;
	/**
	 * validity check field, mfgr-specific<br>
	 * C type : BYTE[2]
	 */
	public byte[] Check = new byte[2];

	/**
	 * Flag to mark the user as already authenticated, e.g. via web server
	 */
	public int Authenticated;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesNamesListHeader32Struct() {
		super();
		//set ALIGN to NONE, because the NAMES_LIST structure is directly followed by the usernames and wildcards in memory
		setAlignType(ALIGN_DEFAULT);
	}
	
	public static NotesNamesListHeader32Struct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesNamesListHeader32Struct>) () -> new NotesNamesListHeader32Struct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"NumNames", //$NON-NLS-1$
			"ID", //$NON-NLS-1$
			"Product", //$NON-NLS-1$
			"Check", //$NON-NLS-1$
			"Authenticated" //$NON-NLS-1$
		);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param numNames number of names in the list
	 * @param id info from LICENSEID, should be empty
	 * @param product info from LICENSEID, should be empty
	 * @param check info from LICENSEID, should be empty
	 * @param authenticated  Flag to mark the user as already authenticated, e.g. via web server
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesNamesListHeader32Struct(short numNames, byte id[], byte product, byte check[], short authenticated) {
		super();
		setAlignType(ALIGN_DEFAULT);
		this.NumNames = numNames;
		if ((id.length != this.ID.length)) {
			throw new WrongArraySizeException("ID"); //$NON-NLS-1$
		}
		this.ID = id;
		this.Product = product;
		if ((check.length != this.Check.length)) {
			throw new WrongArraySizeException("Check"); //$NON-NLS-1$
		}
		this.Check = check;
		this.Authenticated = authenticated;
	}
	
	public static NotesNamesListHeader32Struct newInstance(final short numNames, final byte id[], final byte product, final byte check[], final short authenticated) {
		return AccessController.doPrivileged((PrivilegedAction<NotesNamesListHeader32Struct>) () -> new NotesNamesListHeader32Struct(numNames, id, product, check, authenticated));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesNamesListHeader32Struct(Pointer peer) {
		super(peer);
		setAlignType(ALIGN_DEFAULT);
	}
	
	public static NotesNamesListHeader32Struct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesNamesListHeader32Struct>) () -> new NotesNamesListHeader32Struct(peer));
	}
	
	public static class ByReference extends NotesNamesListHeader32Struct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesNamesListHeader32Struct implements Structure.ByValue {
		
	};
}
