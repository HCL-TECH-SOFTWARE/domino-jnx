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
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesViewTableFormat5Struct extends Structure {
	/** Length of this structure */
	public short Length;
	/** Reserved for future use */
	public int Flags;
	/** see viewprop.h - way to repeat image */
	public short RepeatType;
	
	public NotesViewTableFormat5Struct() {
		super();
	}
	
	public static NotesViewTableFormat5Struct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesViewTableFormat5Struct>) () -> new NotesViewTableFormat5Struct());
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Length", "Flags", "RepeatType"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	/**
	 * @param Length Length of this structure<br>
	 * @param Flags Reserved for future use<br>
	 * @param RepeatType see viewprop.h - way to repeat image
	 */
	public NotesViewTableFormat5Struct(short Length, int Flags, short RepeatType) {
		super();
		this.Length = Length;
		this.Flags = Flags;
		this.RepeatType = RepeatType;
	}
	
	public static NotesViewTableFormat5Struct newInstance(final short Length, final int Flags, final short RepeatType) {
		return AccessController.doPrivileged((PrivilegedAction<NotesViewTableFormat5Struct>) () -> new NotesViewTableFormat5Struct(Length, Flags, RepeatType));
	}

	public NotesViewTableFormat5Struct(Pointer peer) {
		super(peer);
	}
	
	public static NotesViewTableFormat5Struct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesViewTableFormat5Struct>) () -> new NotesViewTableFormat5Struct(peer));
	}

	public static class ByReference extends NotesViewTableFormat5Struct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesViewTableFormat5Struct implements Structure.ByValue {
		
	};
}
