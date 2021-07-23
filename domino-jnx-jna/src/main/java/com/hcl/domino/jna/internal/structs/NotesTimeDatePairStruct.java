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
 * JNA class for the TIMEDATE_PAIR type
 * 
 * @author Karsten Lehmann
 */
public class NotesTimeDatePairStruct extends BaseStructure {
	/** C type : TIMEDATE */
	public NotesTimeDateStruct Lower;
	/** C type : TIMEDATE */
	public NotesTimeDateStruct Upper;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTimeDatePairStruct() {
		super();
	}
	
	public static NotesTimeDatePairStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeDatePairStruct>) () -> new NotesTimeDatePairStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Lower", "Upper"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param Lower C type : TIMEDATE<br>
	 * @param Upper C type : TIMEDATE
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTimeDatePairStruct(NotesTimeDateStruct Lower, NotesTimeDateStruct Upper) {
		super();
		this.Lower = Lower;
		this.Upper = Upper;
	}
	
	public static NotesTimeDatePairStruct newInstance(final NotesTimeDateStruct Lower, final NotesTimeDateStruct Upper) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeDatePairStruct>) () -> new NotesTimeDatePairStruct(Lower, Upper));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesTimeDatePairStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesTimeDatePairStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeDatePairStruct>) () -> new NotesTimeDatePairStruct(p));
	}
	
	public static class ByReference extends NotesTimeDatePairStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesTimeDatePairStruct implements Structure.ByValue {
		
	};
}
