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
	name="CDFILEHEADER",
	members={
		@StructureMember(name="Header", type=LSIG.class),
		@StructureMember(name="FileExtLen", type=short.class, unsigned=true),
		@StructureMember(name="FileDataSize", type=int.class, unsigned=true),
		@StructureMember(name="SegCount", type=int.class, unsigned=true),
		@StructureMember(name="Flags", type=int.class),
		@StructureMember(name="Reserved", type=int.class)
	}
)
public interface CDFileHeader extends RichTextRecord<LSIG> {
	@StructureGetter("Header")
	@Override
	LSIG getHeader();
	
	@StructureGetter("FileExtLen")
	int getFileExtLen();
	@StructureSetter("FileExtLen")
	CDFileHeader setFileExtLen(int fileExtLen);
	
	@StructureGetter("FileDataSize")
	long getFileDataSize();
	@StructureSetter("FileDataSize")
	CDFileHeader setFileDataSize(long fileDataSize);
	
	@StructureGetter("SegCount")
	long getSegCount();
	@StructureSetter("SegCount")
	CDFileHeader setSegCount(long segCount);
}
