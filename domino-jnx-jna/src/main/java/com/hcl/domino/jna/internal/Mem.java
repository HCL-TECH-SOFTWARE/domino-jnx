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
package com.hcl.domino.jna.internal;

import java.util.function.Function;

import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE32;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE64;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE32;
import com.hcl.domino.jna.internal.gc.handles.HANDLE64;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesBlockIdStruct;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

@SuppressWarnings("deprecation")
public class Mem {

	public static short OSMemFree(DHANDLE.ByValue hdl) {
		if (hdl==null || hdl.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be freed");
		}
		short result = NotesCAPI.get().OSMemFree(hdl);
		if (result==0) {
			hdl.setDisposed();
		}
		return result;
	}

	public static short OSMemFree(HANDLE.ByValue hdl) {
		if (hdl==null || hdl.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be freed");
		}
		
		DHANDLE.ByValue hdlByVal = DHANDLE.newInstanceByValue();
		
		if (PlatformUtils.is64Bit()) {
			((DHANDLE64.ByValue)hdlByVal).hdl = ((HANDLE64.ByValue)hdl).hdl;
		}
		else {
			((DHANDLE32.ByValue)hdlByVal).hdl = ((HANDLE32.ByValue)hdl).hdl;
		}
		
		short result = NotesCAPI.get().OSMemFree(hdlByVal);
		if (result==0) {
			hdlByVal.setDisposed();
		}
		return result;
	}

	public static Pointer OSLockObject(NotesBlockIdStruct blockId) {
		DHANDLE.ByValue hdlByVal = DHANDLE.newInstanceByValue();
		
		if (PlatformUtils.is64Bit()) {
			((DHANDLE64.ByValue)hdlByVal).hdl = blockId.pool;
		}
		else {
			((DHANDLE32.ByValue)hdlByVal).hdl = blockId.pool;
		}
		
		if (hdlByVal.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be unlocked");
		}
		
		Pointer poolPtr = NotesCAPI.get().OSLockObject(hdlByVal);

		int block = blockId.block & 0xffff;
		long poolPtrLong = Pointer.nativeValue(poolPtr) + block;
		return new Pointer(poolPtrLong);
	}

	public static boolean OSUnlockObject(NotesBlockIdStruct blockId) {
		DHANDLE.ByValue hdlByVal = DHANDLE.newInstanceByValue();
		
		if (PlatformUtils.is64Bit()) {
			((DHANDLE64.ByValue)hdlByVal).hdl = blockId.pool;
		}
		else {
			((DHANDLE32.ByValue)hdlByVal).hdl = blockId.pool;
		}
		
		if (hdlByVal.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be unlocked");
		}
		
		return NotesCAPI.get().OSUnlockObject(hdlByVal);
	}
	
	public static Pointer OSLockObject(DHANDLE.ByValue hdl) {
		if (hdl==null || hdl.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be locked");
		}
		Pointer ptr = NotesCAPI.get().OSLockObject(hdl);
		return ptr;
	}

	public static Pointer OSLockObject(HANDLE.ByValue hdl) {
		return LockUtil.lockHandle(hdl, (hdlByVal) -> {
			DHANDLE.ByValue dhdlByVal = DHANDLE.newInstanceByValue();
			
			if (PlatformUtils.is64Bit()) {
				((DHANDLE64.ByValue)dhdlByVal).hdl = ((HANDLE64.ByValue)hdlByVal).hdl;
			}
			else {
				((DHANDLE32.ByValue)dhdlByVal).hdl = ((HANDLE32.ByValue)hdlByVal).hdl;
			}
			
			if (dhdlByVal.isNull()) {
				throw new IllegalArgumentException("Null handle cannot be unlocked");
			}
			
			Pointer ptr = NotesCAPI.get().OSLockObject(dhdlByVal);
			return ptr;
		});
	}
	
	public static <T> T OSLockObject(DHANDLE.ByValue handle, Function<Pointer, T> consumer) {
		Pointer p =  OSLockObject(handle);
		try {
			return consumer.apply(p);
		} finally {
			OSUnlockObject(handle);
		}
	}

	public static boolean OSUnlockObject(HANDLE.ByValue hdl) {
		return LockUtil.lockHandle(hdl, (hdlByVal) -> {
			DHANDLE.ByValue dhdlByVal = DHANDLE.newInstanceByValue();
			
			if (PlatformUtils.is64Bit()) {
				((DHANDLE64.ByValue)dhdlByVal).hdl = ((HANDLE64.ByValue)hdlByVal).hdl;
			}
			else {
				((DHANDLE32.ByValue)dhdlByVal).hdl = ((HANDLE32.ByValue)hdlByVal).hdl;
			}
			
			if (dhdlByVal.isNull()) {
				throw new IllegalArgumentException("Null handle cannot be unlocked");
			}
			
			return NotesCAPI.get().OSUnlockObject(dhdlByVal);
		});
	}
	
	public static boolean OSUnlockObject(DHANDLE.ByValue hdl) {
		if (hdl==null || hdl.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be unlocked");
		}
		return NotesCAPI.get().OSUnlockObject(hdl);
	}
	
	public static short OSMemGetSize(DHANDLE.ByValue hdl, IntByReference retSize) {
		if (hdl==null || hdl.isNull()) {
			throw new IllegalArgumentException("Null handle cannot be accessed");
		}
		if (hdl.isDisposed()) {
			throw new ObjectDisposedException(hdl);
		}
		return NotesCAPI.get().OSMemGetSize(hdl, retSize);
	}
	
	public static short OSMemAlloc(
			short  BlkType,
			int  dwSize,
			DHANDLE.ByReference retHandle) {
		return NotesCAPI.get().OSMemAlloc(BlkType, dwSize, retHandle);
	}
	
	public interface LockedMemory extends AutoCloseable {
		Pointer getPointer();
		long getSize();
		@Override void close();
	}
	
	public static class NullLockedMemory implements LockedMemory {
		@Override
		public Pointer getPointer() {
			return null;
		}
		
		@Override
		public long getSize() {
			return 0;
		}
		
		@Override
		public void close() {
			// nothing to close
		}
	}
	
	private static class LockedMemory32 implements LockedMemory {
		private final Pointer pointer;
		private final int handle;
		private final boolean freeAfterClose;
		
		public LockedMemory32(Pointer pointer, int handle, boolean freeAfterClose) {
			this.pointer = pointer;
			this.handle = handle;
			this.freeAfterClose=freeAfterClose;
		}
		
		@Override
		public Pointer getPointer() {
			return pointer;
		}
		
		@Override
		public long getSize() {
			return NotesCAPI.get().OSMemoryGetSize(handle);
		}
		
		@Override
		public void close() {
			try {
				NotesCAPI.get().OSMemoryUnlock(handle);
			}
			finally {
				if (freeAfterClose) {
					NotesCAPI.get().OSMemoryFree(handle);
				}
			}
		}
	}
	
	private static class LockedMemory64 implements LockedMemory {
		private final Pointer pointer;
		private final long handle;
		private final boolean freeAfterClose;
		
		public LockedMemory64(Pointer pointer, long handle, boolean freeAfterClose) {
			this.pointer = pointer;
			this.handle = handle;
			this.freeAfterClose=freeAfterClose;
		}
		
		@Override
		public Pointer getPointer() {
			return pointer;
		}
		
		@Override
		public long getSize() {
			return NotesCAPI.get().OSMemoryGetSize(handle);
		}

		@Override
		public void close() {
			try {
				NotesCAPI.get().OSMemoryUnlock(handle);
			}
			finally {
				if (freeAfterClose) {
					NotesCAPI.get().OSMemoryFree(handle);
				}
			}
		}
	}
	
	public static LockedMemory OSMemoryLock(int handle) { 
		return OSMemoryLock(handle, false);
	}

	public static LockedMemory OSMemoryLock(int handle, boolean freeAfterClose) {
		return new LockedMemory32(NotesCAPI.get().OSMemoryLock(handle), handle, freeAfterClose);
	}
	
	public static LockedMemory OSMemoryLock(long handle) { 
		return OSMemoryLock(handle, false);
	}
	
	public static LockedMemory OSMemoryLock(long handle, boolean freeAfterClose) {
		return new LockedMemory64(NotesCAPI.get().OSMemoryLock(handle), handle, freeAfterClose);
	}
	
	public static LockedMemory OSMemoryLock(DHANDLE.ByReference hdl, boolean freeAfterClose) {
		return LockUtil.lockHandle(hdl, (hdlByVal) -> {
			if (!hdlByVal.isNull()) {
				if (PlatformUtils.is64Bit()) {
					return OSMemoryLock(((DHANDLE64.ByValue)hdlByVal).hdl, freeAfterClose);
				}
				else {
					return OSMemoryLock(((DHANDLE32.ByValue)hdlByVal).hdl, freeAfterClose);
				}
			}
			else {
				return new NullLockedMemory();
			}
		});
	}

	public static boolean OSMemoryUnlock(int handle) {
		return NotesCAPI.get().OSMemoryUnlock(handle);
	}
	
	public static boolean OSMemoryUnlock(long handle) {
		return NotesCAPI.get().OSMemoryUnlock(handle);
	}
	
	public static void OSMemoryFree(int handle) {
		NotesCAPI.get().OSMemoryFree(handle);
	}
	
	public static void OSMemoryFree(long handle) {
		NotesCAPI.get().OSMemoryFree(handle);
	}

	public static int OSMemoryGetSize(int handle) {
		return NotesCAPI.get().OSMemoryGetSize(handle);
	}
	
	public static int OSMemoryGetSize(long handle) {
		return NotesCAPI.get().OSMemoryGetSize(handle);
	}

	public static short OSMemRealloc(DHANDLE.ByValue handle, int newSize) {
		return NotesCAPI.get().OSMemRealloc(handle, newSize);
	}

	public static LockedMemory OSMemoryLock(DHANDLE.ByReference hdl) {
		return OSMemoryLock(hdl, false);
	}
	
}
