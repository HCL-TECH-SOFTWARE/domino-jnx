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

/**
 * JNA class for the LSCOMPILE_ERR_INFO type
 */
public class NotesLSCompileErrorInfo extends BaseStructure {
	/** C type : WORD */
	public short Version;
	/** C type : WORD */
	public short Line;
	/** C type : char* */
	public Pointer pErrText;
	/** C type: char* */
	public Pointer pErrFile;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesLSCompileErrorInfo() {
		super();
		setAlignType(Structure.ALIGN_DEFAULT);
	}
	
	public static NotesLSCompileErrorInfo newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesLSCompileErrorInfo>) NotesLSCompileErrorInfo::new);
	}
	
	@SuppressWarnings("nls")
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Version", "Line", "pErrText", "pErrFile");
	}
	
	/**
	 * @param Version WORD
	 * @param Line WORD
	 * @param pErrText char*
	 * @param pErrFile char*
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesLSCompileErrorInfo(short Version, short Line, Pointer pErrText, Pointer pErrFile) {
		super();
		this.Version = Version;
		this.Line = Line;
		this.pErrText = pErrText;
		this.pErrFile = pErrFile;
		setAlignType(Structure.ALIGN_DEFAULT);
	}
	
	public static NotesLSCompileErrorInfo newInstance(short Version, short Line, Pointer pErrText, Pointer pErrFile) {
		return AccessController.doPrivileged((PrivilegedAction<NotesLSCompileErrorInfo>) () -> new NotesLSCompileErrorInfo(Version, Line, pErrText, pErrFile));
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesLSCompileErrorInfo(Pointer peer) {
		super(peer);
	}
	
	public static NotesLSCompileErrorInfo newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesLSCompileErrorInfo>) () -> new NotesLSCompileErrorInfo(p));
	}
	
	public static class ByReference extends NotesLSCompileErrorInfo implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesLSCompileErrorInfo implements Structure.ByValue {
		
	};
}
