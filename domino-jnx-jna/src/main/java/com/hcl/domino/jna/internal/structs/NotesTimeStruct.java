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
 * JNA class for the TIME type
 * 
 * @author Karsten Lehmann
 */
public class NotesTimeStruct extends BaseStructure {
	/** 1-32767 */
	public int year;
	/** 1-12 */
	public int month;
	/** 1-31 */
	public int day;
	/** 1-7, Sunday is 1 */
	public int weekday;
	/** 0-23 */
	public int hour;
	/** 0-59 */
	public int minute;
	/** 0-59 */
	public int second;
	/** 0-99 */
	public int hundredth;
	/** FALSE or TRUE */
	public int dst;
	/** -11 to +11 */
	public int zone;
	public NotesTimeDateStruct GM;
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTimeStruct() {
		super();
	}
	
	public static NotesTimeStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeStruct>) () -> new NotesTimeStruct());
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"year", //$NON-NLS-1$
			"month", //$NON-NLS-1$
			"day", //$NON-NLS-1$
			"weekday", //$NON-NLS-1$
			"hour", //$NON-NLS-1$
			"minute", //$NON-NLS-1$
			"second", //$NON-NLS-1$
			"hundredth", //$NON-NLS-1$
			"dst", //$NON-NLS-1$
			"zone", //$NON-NLS-1$
			"GM" //$NON-NLS-1$
		);
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesTimeStruct(Pointer peer) {
		super(peer);
	}

	public static NotesTimeStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeStruct>) () -> new NotesTimeStruct(p));
	}

	public static class ByReference extends NotesTimeStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesTimeStruct implements Structure.ByValue {
		
	};
}
