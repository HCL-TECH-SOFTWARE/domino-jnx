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
package com.hcl.domino.jna.internal.gc;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;

public enum JNAGCUtil {
	;
	private static Map<HANDLE, Lock> handleLocks = new WeakHashMap<>();
	private static Map<DHANDLE, Lock> dhandleLocks = new WeakHashMap<>();
	/**
	 * Returns an access lock to ensure exclusive handle access
	 * across threads
	 * 
	 * @param handle handle
	 * @return lock
	 */
	public static Lock getHandleLock(HANDLE handle) {
		synchronized (handleLocks) {
			Lock lock = handleLocks.get(handle);
			if (lock==null) {
				lock = new NotesEnabledReentrantLock();
				handleLocks.put(handle, lock);
			}
			return lock;
		}
	}
	
	/**
	 * Returns an access lock to ensure exclusive handle access
	 * across threads
	 * 
	 * @param handle handle
	 * @return lock
	 */
	public static Lock getHandleLock(DHANDLE handle) {
		synchronized (dhandleLocks) {
			Lock lock = dhandleLocks.get(handle);
			if (lock==null) {
				lock = new NotesEnabledReentrantLock();
				dhandleLocks.put(handle, lock);
			}
			return lock;
		}
	}
	
	private static class NotesEnabledReentrantLock extends ReentrantLock {
		private static final long serialVersionUID = -3961990800361518284L;
		
		@Override
		public void lock() {
			NotesCAPI.get().NotesInitThread();
			super.lock();
		}
		
		@Override
		public void unlock() {
			super.unlock();
			NotesCAPI.get().NotesTermThread();
		}
	}
}
