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
package com.hcl.domino.commons.richtext.structures;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.hcl.domino.commons.richtext.records.MemoryStructureProxy;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

public class GenericResizableMemoryStructure implements ResizableMemoryStructure {
	private ByteBuffer data;
	private final Class<? extends MemoryStructure> recordClass;

	public GenericResizableMemoryStructure(ByteBuffer data, Class<? extends MemoryStructure> recordClass) {
		this.data = data;
		this.recordClass = recordClass;
	}
	
	@Override
	public ByteBuffer getData() {
		return data.duplicate().order(ByteOrder.nativeOrder());
	}
	
	@Override
	public ByteBuffer getVariableData() {
		Class<?> sizeClass = this.recordClass == null ? getClass() : this.recordClass;
		int structureSize = MemoryStructureProxy.sizeOf(sizeClass);
		return ((ByteBuffer) getData().position(structureSize)).slice().order(ByteOrder.nativeOrder());
	}

	@Override
	public void resize(int size) {
		if(size < 1) {
			throw new IllegalArgumentException("New size must be greater than 0 bytes");
		}
		ByteBuffer newData = ByteBuffer.allocate(size);
		int copySize = Math.min(size, data.capacity());
		data.position(0);
		data.limit(copySize);
		newData.put(data);
		newData.position(0);
		this.data = newData;
	}
	
	@Override
	public void resizeVariableData(int size) {
		if(size < 1) {
			throw new IllegalArgumentException("New size must be greater than 0 bytes");
		}
		Class<?> sizeClass = this.recordClass == null ? getClass() : this.recordClass;
		int totalSize = MemoryStructureProxy.sizeOf(sizeClass) + size;
		resize(totalSize);
	}
}
