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
package com.hcl.domino.richtext.structures;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
	name="CROPRECT",
	members={
		@StructureMember(name="top", type=short.class, unsigned=true),
		@StructureMember(name="left", type=short.class, unsigned=true),
		@StructureMember(name="right", type=short.class, unsigned=true),
		@StructureMember(name="bottom", type=short.class, unsigned=true)
	}
)
public interface CropRect extends MemoryStructure {
	@StructureGetter("top")
	int getTop();
	@StructureSetter("top")
	CropRect setTop(int top);
	
	@StructureGetter("left")
	int getLeft();
	@StructureSetter("left")
	CropRect setLeft(int left);
	
	@StructureGetter("right")
	int getRight();
	@StructureSetter("right")
	CropRect setRight(int right);
	
	@StructureGetter("bottom")
	int getBottom();
	@StructureSetter("bottom")
	CropRect setBottom(int bottom);
}
