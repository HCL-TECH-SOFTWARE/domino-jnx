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
package com.hcl.domino.jna.dxl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.misc.INumberEnum;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * Contains logic common between DXL importers and exporters.
 * 
 * @author Jesse Gallagher
 */
@SuppressWarnings("rawtypes")
abstract class AbstractDxlProcessor<AT extends APIObjectAllocations, PROP extends Enum & INumberEnum<Integer>> extends BaseJNAAPIObject<AT> {

	public AbstractDxlProcessor(IAPIObject<?> parent) {
		super(parent);
	}


	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	protected abstract void getProperty(int hDxl, INumberEnum<Integer> prop, Pointer retPropValue);
	
	protected abstract void setProperty(int hDxl, INumberEnum<Integer> prop, Pointer propValue);
	
	/**
	 * Checks whether the previous operation resulted in an error and, if so, throws an encapsulated
	 * exception.
	 */
	protected abstract void checkError();
	
	protected abstract int getHandle();


	protected boolean isProp(PROP prop) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		IntByReference ref = new IntByReference();
		getProperty(hDXLExport, prop, ref.getPointer());
		return ref.getValue() == 1;
	}


	protected void setProp(PROP prop, boolean value) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		IntByReference ref = new IntByReference(value ? 1 : 0);
		setProperty(hDXLExport, prop, ref.getPointer());
	}


	protected String getPropString(PROP prop) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		IntByReference memhandle = new IntByReference();
		getProperty(hDXLExport, prop, memhandle.getPointer());
		try(LockedMemory lmbcs = Mem.OSMemoryLock(memhandle.getValue())) {
			return NotesStringUtils.fromLMBCS(lmbcs.getPointer(), -1);
		} finally {
			Mem.OSMemoryFree(memhandle.getValue());
		}
	}


	protected void setProp(PROP prop, String value) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		Memory lmbcs = NotesStringUtils.toLMBCS(String.valueOf(value), true);
		setProperty(hDXLExport, prop, lmbcs);
	}


	protected List<String> getPropStringList(PROP prop) {
		checkDisposed();
		int hDXLExport = getHandle();
	
		
		HANDLE.ByValue hList = HANDLE.newInstanceByValue();
		getProperty(hDXLExport, prop, hList.getAdapter(Pointer.class));
		hList.getAdapter(Structure.class).read();
		try {
			Pointer mem = Mem.OSLockObject(hList);
			try {
				int length = Short.toUnsignedInt(NotesCAPI.get().ListGetNumEntries(mem, 0));
				List<String> result = new ArrayList<>(length);
				for(int i = 0; i < length; i++) {
					Memory retTextPointer = new Memory(Native.POINTER_SIZE);
					ShortByReference retTextLength = new ShortByReference();
					
					NotesErrorUtils.checkResult(
					    NotesCAPI.get().ListGetText(mem, false, (char) i, retTextPointer, retTextLength)
					    );
					result.add(NotesStringUtils.fromLMBCS(retTextPointer.getPointer(0), Short.toUnsignedInt(retTextLength.getValue())));
				}
				return result;
			} finally {
				Mem.OSUnlockObject(hList);
			}
		} finally {
			if(hList != null && !hList.isNull()) {
				Mem.OSMemFree(hList);
			}
		}
	}


	protected void setProp(PROP prop, List<String> valueParam) {
		checkDisposed();
		int hDXLExport = getHandle();
		List<String> value = valueParam == null ? Collections.emptyList() : valueParam;

    if (value.size()>65535) {
      throw new DominoException(MessageFormat.format("List size exceeds max value of 65535 entries: {0}", value.size()));
    }

		DHANDLE.ByReference hList = DHANDLE.newInstanceByReference();
		Memory retpList = new Memory(Native.POINTER_SIZE);
		ShortByReference retListSize = new ShortByReference();
		NotesCAPI.get().ListAllocate((short)0, (short)0, 0, hList, retpList, retListSize);
		try {
			for(int i = 0; i < value.size(); i++) {
				Memory lmbcs = NotesStringUtils.toLMBCS(String.valueOf(value.get(i)), false);
				
        if (lmbcs!=null && lmbcs.size() > 65535) {
          throw new DominoException(MessageFormat.format("List item at position {0} exceeds max lengths of 65535 bytes", i));
        }

        char textSize = lmbcs==null ? 0 : (char) lmbcs.size();

        NotesErrorUtils.checkResult(
            NotesCAPI.get().ListAddEntry(
                DHANDLE.newInstanceByValue(hList),
                0,
                retListSize,
                (char) i,
                lmbcs,
                textSize
                ));
			}

			setProperty(hDXLExport, prop, hList.getAdapter(Pointer.class));
		} finally {
			Mem.OSMemFree(DHANDLE.newInstanceByValue(hList));
		}
	}


	protected int getPropInt(PROP prop) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		IntByReference ref = new IntByReference();
		getProperty(hDXLExport, prop, ref.getPointer());
		return ref.getValue();
	}


	protected void setProp(PROP prop, int value) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		IntByReference ref = new IntByReference(value);
		setProperty(hDXLExport, prop, ref.getPointer());
	}
	
	protected DHANDLE getPropDHANDLE(PROP prop) {
		checkDisposed();
		int hDXLExport = getHandle();
		
		DHANDLE.ByReference result = DHANDLE.newInstanceByReference();
		getProperty(hDXLExport, prop, result.getAdapter(Pointer.class));
		result.getAdapter(Structure.class).read();
		return result;
	}
}
