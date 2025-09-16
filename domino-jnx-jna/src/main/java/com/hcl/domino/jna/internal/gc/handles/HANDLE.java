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
package com.hcl.domino.jna.internal.gc.handles;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.exception.ObjectDisposedException;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface HANDLE extends IAdaptable, IHANDLEBase<HANDLE,HANDLE.ByValue> {
	
	@Override
	<R> R _lockHandleAccess(HandleAccess<HANDLE.ByValue,R> handleAccess);
	
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
	 * @return the handle value
	 * @since 1.48.0
	 */
	long getValue();

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
	static HANDLE newInstance(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<HANDLE>) () -> {
		  return new HANDLE64(peer);
		});
	}
	
	@SuppressWarnings("deprecation")
	static HANDLE newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<HANDLE>) () -> {
		  return new HANDLE64();
		});
	}
	
	@SuppressWarnings("deprecation")
	static HANDLE.ByReference newInstanceByReference(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> {
		  return new HANDLE64.ByReference(peer);
		});
	}
	
	@SuppressWarnings("deprecation")
	static HANDLE.ByReference newInstanceByReference() {
		return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> {
		  return new HANDLE64.ByReference();
		});
	}
	
	@SuppressWarnings("deprecation")
	static HANDLE.ByReference newInstanceByReference(HANDLE hdlToCopy) {
		return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> {
		  HANDLE64.ByReference newHdl = new HANDLE64.ByReference();
          newHdl.hdl = ((HANDLE64)hdlToCopy).hdl;
          newHdl.write();
          return newHdl;
		});
	}
	
	@SuppressWarnings("deprecation")
	static ByValue newInstanceByValue() {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> {
		  return new HANDLE64.ByValue();
		});
	}
	
	@SuppressWarnings("deprecation")
	static ByValue newInstanceByValue(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> {
		  return new HANDLE64.ByValue(peer);
		});
	}
	
	public interface ByReference extends HANDLE, Structure.ByReference {
		
	}

	public interface ByValue extends HANDLE, Structure.ByValue {
		
	}

	@Override boolean isNull();
	
}
