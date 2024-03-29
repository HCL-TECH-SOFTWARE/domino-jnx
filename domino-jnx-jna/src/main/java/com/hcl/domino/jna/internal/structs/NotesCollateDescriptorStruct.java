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
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesCollateDescriptorStruct extends BaseStructure {
	public byte Flags;
	/** Must be COLLATE_DESCRIPTOR_SIGNATURE */
	public byte signature;
	/** Type of key (COLLATE_TYPE_xxx) */
	public byte keytype;
	/** Offset to the name string */
	public short NameOffset;
	/** Length of the name string */
	public short NameLength;
	public NotesCollateDescriptorStruct() {
		super();
		setAlignType(Structure.ALIGN_NONE);
	}
	
	public static NotesCollateDescriptorStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollateDescriptorStruct>) () -> new NotesCollateDescriptorStruct());
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"Flags", //$NON-NLS-1$
			"signature", //$NON-NLS-1$
			"keytype", //$NON-NLS-1$
			"NameOffset", //$NON-NLS-1$
			"NameLength" //$NON-NLS-1$
		);
	}
	/**
	 * @param Flags flags
	 * @param signature Must be COLLATE_DESCRIPTOR_SIGNATURE<br>
	 * @param keytype Type of key (COLLATE_TYPE_xxx)<br>
	 * @param NameOffset Offset to the name string<br>
	 * @param NameLength Length of the name string
	 */
	public NotesCollateDescriptorStruct(byte Flags, byte signature, byte keytype, short NameOffset, short NameLength) {
		super();
		this.Flags = Flags;
		this.signature = signature;
		this.keytype = keytype;
		this.NameOffset = NameOffset;
		this.NameLength = NameLength;
		setAlignType(Structure.ALIGN_NONE);
	}
	
	public static NotesCollateDescriptorStruct newInstance(final byte Flags, final byte signature, final byte keytype, final short NameOffset, final short NameLength) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollateDescriptorStruct>) () -> new NotesCollateDescriptorStruct(Flags, signature, keytype, NameOffset, NameLength));
	}

	public NotesCollateDescriptorStruct(Pointer peer) {
		super(peer);
		setAlignType(Structure.ALIGN_NONE);
	}
	
	public static NotesCollateDescriptorStruct newInstance(final Pointer p) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollateDescriptorStruct>) () -> new NotesCollateDescriptorStruct(p));
	}

	public static class ByReference extends NotesCollateDescriptorStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesCollateDescriptorStruct implements Structure.ByValue {
		
	};
}
