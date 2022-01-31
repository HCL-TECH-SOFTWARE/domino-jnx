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

/**
 * Callback interface to ensure exclusive handle access across threads.
 * 
 * @author Karsten Lehmann
 *
 * @param <T> lock type
 * @param <R> result type
 */
public interface HandleAccess<T,R> {

	/**
	 * Implement this method with code accessing the handle (calling
	 * C API methods with the handle as argument)
	 * 
	 * @param handle handle
	 * @return result
	 */
	R accessLockedHandle(T handle);
	
}
