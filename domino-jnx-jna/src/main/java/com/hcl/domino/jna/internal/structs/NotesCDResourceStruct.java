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

import com.hcl.domino.data.IAdaptable;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
/**
 * This CD record defines a resource within a database.<br>
 * There may be many resources defined within a particular database.<br>
 * A resource can be an image, an applet, a shared field or a script library.
 */
public class NotesCDResourceStruct extends BaseStructure implements IAdaptable {
	/** ORed with WORDRECORDLENGTH */
	public short Signature;
	/** (length is inclusive with this struct) */
	public short Length;
	/** one of CDRESOURCE_FLAGS_xxx */
	public int Flags;
	/** one of CDRESOURCE_TYPE_xxx */
	public short Type;
	/** one of CDRESOURCE_CLASS_xxx */
	public short ResourceClass;
	/** meaning depends on Type */
	public short Length1;
	/** length of the server hint */
	public short ServerHintLength;
	/** length of the file hint */
	public short FileHintLength;
	/** C type : BYTE[8] */
	public byte[] Reserved = new byte[8];
	
	public NotesCDResourceStruct() {
		super();
	}

	public static NotesCDResourceStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesCDResourceStruct>) () -> new NotesCDResourceStruct());
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getFieldOrder() {
		return Arrays.asList("Signature", "Length", "Flags", "Type", "ResourceClass", "Length1", "ServerHintLength", "FileHintLength", "Reserved");
	}
	
	public NotesCDResourceStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesCDResourceStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCDResourceStruct>) () -> new NotesCDResourceStruct(peer));
	}

	public static class ByReference extends NotesCDResourceStruct implements Structure.ByReference {
		
	};
	
	public static class ByValue extends NotesCDResourceStruct implements Structure.ByValue {
		
	};
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesCDResourceStruct.class) {
			return (T) this;
		}
		return null;
	}

}
