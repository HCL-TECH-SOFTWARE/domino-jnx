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
package com.hcl.domino.jna.internal.gc.handles;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.exception.ObjectDisposedException;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface DHANDLE extends IAdaptable, IHANDLEBase<DHANDLE,DHANDLE.ByValue> {

	@Override
	<R> R _lockHandleAccess(HandleAccess<DHANDLE.ByValue,R> handleAccess);

	/**
	 * Returns whether the handle is disposed
	 * 
	 * @return true if disposed
	 */
	boolean isDisposed();
	
	/**
	 * Marks the handle as disposed
	 */
	void setDisposed();
	
	/**
	 * Fill handle with a null value
	 */
	void clear();
	
	/**
	 * Returns the handle size in memory
	 * 
	 * @return size
	 */
	int size();
	
	/**
	 * Throws a {@link DominoException} if the handle is marked as disposed
	 */
	@Override
	default void checkDisposed() {
		if (isDisposed()) {
			throw new ObjectDisposedException(this);
		}
	}
	
	@SuppressWarnings("deprecation")
	static DHANDLE newInstance(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<DHANDLE>) () -> {
			if (PlatformUtils.is64Bit()) {
				return new DHANDLE64(peer);
				
			}
			else {
				return new DHANDLE32(peer);
				
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	static DHANDLE newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<DHANDLE>) () -> {
			if (PlatformUtils.is64Bit()) {
				return new DHANDLE64();
				
			}
			else {
				return new DHANDLE32();
				
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	static DHANDLE.ByReference newInstanceByReference(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> {
			if (PlatformUtils.is64Bit()) {
				return new DHANDLE64.ByReference(peer);
				
			}
			else {
				return new DHANDLE32.ByReference(peer);
				
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	static DHANDLE.ByReference newInstanceByReference() {
		return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> {
			if (PlatformUtils.is64Bit()) {
				return new DHANDLE64.ByReference();
				
			}
			else {
				return new DHANDLE32.ByReference();
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	static ByValue newInstanceByValue() {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> {
			if (PlatformUtils.is64Bit()) {
				return new DHANDLE64.ByValue();
				
			}
			else {
				return new DHANDLE32.ByValue();
				
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	static ByValue newInstanceByValue(DHANDLE copyHandleValueFrom) {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> {
			if (PlatformUtils.is64Bit()) {
				DHANDLE64.ByValue newHdl1 = new DHANDLE64.ByValue();
				newHdl1.hdl = ((DHANDLE64)copyHandleValueFrom).hdl;
				return newHdl1;
			}
			else {
				DHANDLE32.ByValue newHdl2 = new DHANDLE32.ByValue();
				newHdl2.hdl = ((DHANDLE32)copyHandleValueFrom).hdl;
				return newHdl2;
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	static ByValue newInstanceByValue(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> {
			if (PlatformUtils.is64Bit()) {
				return new DHANDLE64.ByValue(peer);
				
			}
			else {
				return new DHANDLE32.ByValue(peer);
				
			}
		});
	}
	
	public interface ByReference extends DHANDLE, Structure.ByReference {
		
	}

	public interface ByValue extends DHANDLE, Structure.ByValue {
		
	}

	@Override boolean isNull();
	
	public Pointer getPointer();
	
}
