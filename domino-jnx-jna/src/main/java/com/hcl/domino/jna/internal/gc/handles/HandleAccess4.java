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

/**
 * Callback interface to ensure exclusive handle access across threads.
 * 
 * @author Karsten Lehmann
 *
 * @param <T1BYVAL> lock type 1
 * @param <T2BYVAL> lock type 2
 * @param <T3BYVAL> lock type 3
 * @param <T4BYVAL> lock type 4
 * @param <R> result type
 */
public interface HandleAccess4<T1BYVAL,T2BYVAL,T3BYVAL,T4BYVAL, R> {

	/**
	 * Implement this method with code accessing the handles (calling
	 * C API methods with the handle as argument)
	 * 
	 * @param handle1 handle 1
	 * @param handle2 handle 2
	 * @param handle3 handle 3
	 * @param handle4 handle 4
	 * @return result
	 */
	R accessLockedHandles(T1BYVAL handle1, T2BYVAL handle2, T3BYVAL handle3, T4BYVAL handle4);
	
}
