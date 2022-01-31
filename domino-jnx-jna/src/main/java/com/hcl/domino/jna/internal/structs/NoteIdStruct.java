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

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.data.IAdaptable;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * JNA class for the NOTEID type
 * 
 * @author Karsten Lehmann
 */
public class NoteIdStruct extends BaseStructure implements Serializable, IAdaptable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	public int nid;
	
	public static NoteIdStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NoteIdStruct>) () -> new NoteIdStruct());
	}
	
	public static NoteIdStruct newInstance(final int nid) {
		return AccessController.doPrivileged((PrivilegedAction<NoteIdStruct>) () -> new NoteIdStruct(nid));
	}
	
	public static NoteIdStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NoteIdStruct>) () -> {
			NoteIdStruct newObj = new NoteIdStruct(peer);
			newObj.read();
			return newObj;
		});
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NoteIdStruct() {
		super();
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("nid"); //$NON-NLS-1$
	}
		
	/**
	 * Creates a new instance
	 * 
	 * @param nid note id
	 */
	public NoteIdStruct(int nid) {
		super();
		this.nid = nid;
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NoteIdStruct(Pointer peer) {
		super(peer);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NoteIdStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static class ByReference extends NoteIdStruct implements Structure.ByReference {
		private static final long serialVersionUID = -3097461571616131768L;
		
	};
	public static class ByValue extends NoteIdStruct implements Structure.ByValue {
		private static final long serialVersionUID = -5045877402293096954L;
		
	};
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NoteIdStruct) {
			return this.nid == ((NoteIdStruct)o).nid;
		}
		return false;
	}
	
	/**
	 * Creates a new {@link NoteIdStruct} instance with the same data as this one
	 */
	@Override
	@SuppressFBWarnings(value="CN_IDIOM_NO_SUPER_CALL", justification="Dealing with native memory")
	public NoteIdStruct clone() {
		NoteIdStruct clone = new NoteIdStruct();
		clone.nid = this.nid;
		clone.write();
		return clone;
	}
	
}
