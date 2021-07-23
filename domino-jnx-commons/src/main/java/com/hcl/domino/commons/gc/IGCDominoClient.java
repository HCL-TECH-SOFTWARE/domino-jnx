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
package com.hcl.domino.commons.gc;

import com.hcl.domino.DominoClient;
import com.hcl.domino.commons.IDefaultDominoClient;

/**
 * This interface represents a {@link DominoClient} implementation that, in addition to the behaviors
 * specified in {@link IDefaultDominoClient}, also acts as a root for automatic garbage collection.
 *
 * @param <AT> the {@link APIObjectAllocations} type used by the implementation
 * @since 1.0.19
 */
@SuppressWarnings("rawtypes")
public interface IGCDominoClient<AT extends APIObjectAllocations> extends IDefaultDominoClient, IAPIObject<AT> {	
	void markRegisteredForGC();
	boolean isRegisteredForGC();
	
	boolean isAllowCrossThreadAccess();
	
	@SuppressWarnings("unchecked")
	@Override
	default <T> T getAdapter(Class<T> clazz) {
		if(clazz == IGCDominoClient.class) {
			return (T)this;
		}
		return null;
	}
}
