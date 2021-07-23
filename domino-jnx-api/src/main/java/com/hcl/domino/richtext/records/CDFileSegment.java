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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
	name="CDFILESEGMENT",
	members={
		@StructureMember(name="Header", type=LSIG.class),
		@StructureMember(name="DataSize", type=short.class, unsigned=true),
		@StructureMember(name="SegSize", type=short.class, unsigned=true),
		@StructureMember(name="Flags", type=int.class),
		@StructureMember(name="Reserved", type=int.class)
	}
)
public interface CDFileSegment extends RichTextRecord<LSIG> {
	@StructureGetter("Header")
	@Override
	LSIG getHeader();
	
	@StructureGetter("DataSize")
	int getDataSize();
	@StructureSetter("DataSize")
	CDFileSegment setDataSize(int dataSize);
	
	@StructureGetter("SegSize")
	int getSegSize();
	@StructureSetter("SegSize")
	CDFileSegment setSegSize(int segSize);
	
	default byte[] getFileSegmentData() {
		ByteBuffer buf = getVariableData();
		int len = getDataSize();
		byte[] data = new byte[len];
		buf.get(data);
		return data;
	}
	
	default CDFileSegment setFileSegmentData(byte[] data) {
		ByteBuffer buf = getVariableData();
		buf.put(data);
		int remaining = buf.remaining();
		for(int i = 0; i < remaining; i++) {
			buf.put((byte)0);
		}
		return this;
	}
}
