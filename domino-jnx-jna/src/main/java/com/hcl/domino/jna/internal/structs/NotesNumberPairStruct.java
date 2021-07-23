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
 * JNA class for the NUMBER_PAIR type
 * 
 * @author Karsten Lehmann
 */
public class NotesNumberPairStruct extends BaseStructure {
	/** C type : NUMBER */
	public double Lower;
	/** C type : NUMBER */
	public double Upper;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesNumberPairStruct() {
		super();
	}
	
	public static NotesNumberPairStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesNumberPairStruct>) () -> new NotesNumberPairStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Lower", "Upper"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @param Lower C type : NUMBER<br>
	 * @param Upper C type : NUMBER
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesNumberPairStruct(double Lower, double Upper) {
		super();
		this.Lower = Lower;
		this.Upper = Upper;
	}
	
	public static NotesNumberPairStruct newInstance(final double Lower, final double Upper) {
		return AccessController.doPrivileged((PrivilegedAction<NotesNumberPairStruct>) () -> new NotesNumberPairStruct(Lower, Upper));
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesNumberPairStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesNumberPairStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesNumberPairStruct>) () -> new NotesNumberPairStruct(p));
	}
	
	public static class ByReference extends NotesNumberPairStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesNumberPairStruct implements Structure.ByValue {
		
	};
}
