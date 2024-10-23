/*
 * ==========================================================================
 * Copyright (C) 2019-2024 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.data.structures;

import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * Represents aggregate data about a view or folder.
 * 
 * @since 1.44.0
 */
@StructureDefinition(
    name = "COLLECTIONDATA",
    members = {
      @StructureMember(name = "DocCount", type = int.class, unsigned = true),
      @StructureMember(name = "DocTotalSize", type = int.class, unsigned = true),
      @StructureMember(name = "BTreeLeafNodes", type = int.class, unsigned = true),
      @StructureMember(name = "BTreeDepth", type = short.class, unsigned = true),
      @StructureMember(name = "Spare", type = short.class),
      @StructureMember(name = "KeyOffset", type = int[].class, unsigned = true, length = NotesConstants.PERCENTILE_COUNT)
    }
  )
public interface CollectionData extends MemoryStructure {
  @StructureGetter("DocCount")
  long getDocCount();
  
  @StructureGetter("DocTotalSize")
  long getDocTotalSize();
  
  @StructureGetter("BTreeLeafNodes")
  long getLeafNodeCount();
  
  @StructureGetter("BTreeDepth")
  int getDepth();
}
