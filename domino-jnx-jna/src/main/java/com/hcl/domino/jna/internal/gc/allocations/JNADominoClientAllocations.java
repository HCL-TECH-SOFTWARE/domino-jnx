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
package com.hcl.domino.jna.internal.gc.allocations;

import java.lang.ref.ReferenceQueue;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.jna.JNADominoClient;

public class JNADominoClientAllocations extends APIObjectAllocations<JNADominoClient> {
	private boolean disposed;

	@SuppressWarnings("rawtypes")
	public JNADominoClientAllocations(JNADominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNADominoClient referent, ReferenceQueue<? super IAPIObject> q) {
		
		super(parentDominoClient, parentAllocations, referent, q);
	}
	
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public void dispose() {
		if (isDisposed()) {
			return;
		}

		disposed = true;
	}

}
