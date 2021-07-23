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

public class LockUtil {

	/**
	 * Method to lock one handle
	 * 
	 * @param <T1> type of handle 1
	 * @param <T1BYVAL> type of handle 1 by val
	 * @param <R> result type
	 * @param handle1 handle or null
	 * @param callback callback
	 * @return result
	 */
	public static <T1, T1BYVAL, R> R lockHandle(IHANDLEBase<T1,T1BYVAL> handle1, HandleAccess<T1BYVAL,R> callback) {
		if (handle1==null || handle1.isNull()) {
			handle1 = new NullHandle<>();
		}
		final IHANDLEBase<T1,T1BYVAL> fHandle1 = handle1;
		
		return fHandle1._lockHandleAccess((handle1ByVal) -> {
			return callback.accessLockedHandle(handle1ByVal);
		});
	}
	
	/**
	 * Method to lock two handles
	 * 
	 * @param <T1> type of handle 1
	 * @param <T1BYVAL> type of handle 1 by val
	 * @param <T2> type of handle 2
	 * @param <T2BYVAL> type of handle 2 by val
	 * @param <R> result type
	 * @param handle1 handle 1 or null
	 * @param handle2 handle 2 or null
	 * @param callback callback
	 * @return result
	 */
	public static <T1,T1BYVAL,T2,T2BYVAL, R> R lockHandles(IHANDLEBase<T1,T1BYVAL> handle1, IHANDLEBase<T2,T2BYVAL> handle2,
			HandleAccess2<T1BYVAL,T2BYVAL,R> callback) {
		
		if (handle1==null || handle1.isNull()) {
			handle1 = new NullHandle<>();
		}
		if (handle2==null || handle2.isNull()) {
			handle2 = new NullHandle<>();
		}
		final IHANDLEBase<T1,T1BYVAL> fHandle1 = handle1;
		final IHANDLEBase<T2,T2BYVAL> fHandle2 = handle2;
		
		return fHandle1._lockHandleAccess((handle1ByVal) -> {
			return fHandle2._lockHandleAccess((handle2ByVal) -> {
				return callback.accessLockedHandles(handle1ByVal, handle2ByVal);
			});
		});
	}
	
	/**
	 * Method to lock three handles
	 * 
	 * @param <T1> type of handle 1
	 * @param <T1BYVAL> type of handle 2 by val
	 * @param <T2> type of handle 2
	 * @param <T2BYVAL> type of handle 2 by val
	 * @param <T3> type of handle 3
	 * @param <T3BYVAL> type of handle 3 by val
	 * @param <R> result type
	 * @param handle1 handle 1 or null
	 * @param handle2 handle 2 or null
	 * @param handle3 handle 3 or null
	 * @param callback callback
	 * @return result
	 */
	public static <T1,T1BYVAL,T2,T2BYVAL,T3,T3BYVAL,R> R lockHandles(IHANDLEBase<T1,T1BYVAL> handle1, IHANDLEBase<T2,T2BYVAL> handle2,
			IHANDLEBase<T3,T3BYVAL> handle3, HandleAccess3<T1BYVAL,T2BYVAL,T3BYVAL,R> callback) {
		
		if (handle1==null || handle1.isNull()) {
			handle1 = new NullHandle<>();
		}
		if (handle2==null || handle2.isNull()) {
			handle2 = new NullHandle<>();
		}
		if (handle3==null || handle3.isNull()) {
			handle3 = new NullHandle<>();
		}
		
		final IHANDLEBase<T1,T1BYVAL> fHandle1 = handle1;
		final IHANDLEBase<T2,T2BYVAL> fHandle2 = handle2;
		final IHANDLEBase<T3,T3BYVAL> fHandle3 = handle3;
		
		return fHandle1._lockHandleAccess((handle1ByVal) -> {
			return fHandle2._lockHandleAccess((handle2ByVal) -> {
				return fHandle3._lockHandleAccess((handle3ByVal) -> {
					return callback.accessLockedHandles(handle1ByVal, handle2ByVal, handle3ByVal);
				});
			});
		});
	}
	
	/**
	 * Method to lock four handles
	 * 
	 * @param <T1> type of handle 1
	 * @param <T1BYVAL> type of handle 2 by val
	 * @param <T2> type of handle 2
	 * @param <T2BYVAL> type of handle 2 by val
	 * @param <T3> type of handle 3
	 * @param <T3BYVAL> type of handle 3 by val
	 * @param <T4> type of handle 4
	 * @param <T4BYVAL> type of handle 4 by val
	 * @param <R> result type
	 * 
	 * @param handle1 handle 1 or null
	 * @param handle2 handle 2 or null
	 * @param handle3 handle 3 or null
	 * @param handle4 handle 4 or null
	 * @param callback callback
	 * @return result
	 */
	public static <T1,T1BYVAL,T2,T2BYVAL,T3,T3BYVAL,T4,T4BYVAL,R> R lockHandles(
			IHANDLEBase<T1,T1BYVAL> handle1,
			IHANDLEBase<T2,T2BYVAL> handle2,
			IHANDLEBase<T3,T3BYVAL> handle3,
			IHANDLEBase<T4,T4BYVAL> handle4,
			HandleAccess4<T1BYVAL,T2BYVAL,T3BYVAL,T4BYVAL, R> callback) {
		
		if (handle1==null || handle1.isNull()) {
			handle1 = new NullHandle<>();
		}
		if (handle2==null || handle2.isNull()) {
			handle2 = new NullHandle<>();
		}
		if (handle3==null || handle3.isNull()) {
			handle3 = new NullHandle<>();
		}
		if (handle4==null || handle4.isNull()) {
			handle4 = new NullHandle<>();
		}
		
		final IHANDLEBase<T1,T1BYVAL> fHandle1 = handle1;
		final IHANDLEBase<T2,T2BYVAL> fHandle2 = handle2;
		final IHANDLEBase<T3,T3BYVAL> fHandle3 = handle3;
		final IHANDLEBase<T4,T4BYVAL> fHandle4 = handle4;
		
		return fHandle1._lockHandleAccess((handle1ByVal) -> {
			return fHandle2._lockHandleAccess((handle2ByVal) -> {
				return fHandle3._lockHandleAccess((handle3ByVal) -> {
					return fHandle4._lockHandleAccess((handle4ByVal) -> {
						return callback.accessLockedHandles(handle1ByVal, handle2ByVal, handle3ByVal, handle4ByVal);
					});
				});
			});
		});
	}
	
	
	private static class NullHandle<LOCKTYPE, LOCKBYVALTYPE> implements IHANDLEBase<LOCKTYPE, LOCKBYVALTYPE> {

		@Override
		public <R> R _lockHandleAccess(HandleAccess<LOCKBYVALTYPE, R> handleAccess) {
			return AccessController.doPrivileged((PrivilegedAction<R>) () -> handleAccess.accessLockedHandle(null));
		}
		
		@Override
		public void checkDisposed() {
		}
		
		@Override
		public boolean isNull() {
			return true;
		}
	}

}
