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
package com.hcl.domino.jna.internal;

import com.sun.jna.Memory;

/**
 * Subclass of {@link Memory} that can explicitly be disposed to reduce memory usage.
 * 
 * @author Karsten Lehmann
 */
public class DisposableMemory extends Memory implements AutoCloseable {

	/**
	 * Allocate space in the native heap via a call to C's <code>malloc</code>.
	 *
	 * @param size number of <em>bytes</em> of space to allocate
	 */
	public DisposableMemory(long size) {
		super(size);
		clear();
	}
	
	/**
	 * Checks if this memory is already disposed
	 * 
	 * @return true if disposed
	 */
	public boolean isDisposed() {
		return peer == 0;
	}
	
	@Override
	public void close() {
		dispose();
	}
	
}
