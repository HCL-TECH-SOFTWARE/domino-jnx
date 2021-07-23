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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;

/**
 * Extension of {@link Structure} to make it work in an execution environment
 * that is secured by the Domino SecurityManager.
 * 
 * @author Karsten Lehmann
 */
public abstract class BaseStructure extends Structure {
	
	protected BaseStructure() {
		super(NotesCAPI.getPlatformAlignment());
		int overrideAlignment = getOverrideAlignment();
		if (overrideAlignment!=-1) {
			setAlignType(overrideAlignment);
		}
	}
	
	public BaseStructure(int alignType, TypeMapper mapper) {
		super(alignType, mapper);
	}

	public BaseStructure(int alignType) {
		super(alignType);
	}

	public BaseStructure(Pointer p, int alignType, TypeMapper mapper) {
		super(p, alignType, mapper);
	}

	public BaseStructure(Pointer p, int alignType) {
		super(p, alignType);
	}

	public BaseStructure(Pointer p) {
		super(p, NotesCAPI.getPlatformAlignment());
		int overrideAlignment = getOverrideAlignment();
		if (overrideAlignment!=-1) {
			setAlignType(overrideAlignment);
		}
	}

	public BaseStructure(TypeMapper mapper) {
		super(mapper);
		setAlignType(NotesCAPI.getPlatformAlignment());
		int overrideAlignment = getOverrideAlignment();
		if (overrideAlignment!=-1) {
			setAlignType(overrideAlignment);
		}
	}
	
	protected int getOverrideAlignment() {
		return -1;
	}
	
	@Override
	protected List<Field> getFieldList() {
		return AccessController.doPrivileged((PrivilegedAction<List<Field>>) () -> BaseStructure.super.getFieldList());
	}
	
	@Override
	public void read() {
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			BaseStructure.super.read();
			return null;
		});
	}
	
	@Override
	public void write() {
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			BaseStructure.super.write();
			return null;
		});
	}
	
}
