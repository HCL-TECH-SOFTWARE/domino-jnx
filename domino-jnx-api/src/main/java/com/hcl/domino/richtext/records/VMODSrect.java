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
package com.hcl.domino.richtext.records;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * VMODSrect
 * 
 * @author artcnot
 * @since 1.0.38
 */
@StructureDefinition(
  name = "VMODSrect",
  members = {
    @StructureMember(name = "left", type = int.class, unsigned = true),
    @StructureMember(name = "top", type =  int.class, unsigned = true),
    @StructureMember(name = "right", type =  int.class, unsigned = true),
    @StructureMember(name = "bottom", type =  int.class, unsigned = true)
  }
)
public interface VMODSrect extends MemoryStructure {
  @StructureGetter("left")
  long getLeft();

  @StructureGetter("top")
  long getTop();

  @StructureGetter("right")
  long getRight();

  @StructureGetter("bottom")
  long getBottom();

  @StructureSetter("left")
  VMODSrect setLeft(long left);

  @StructureSetter("top")
  VMODSrect setTop(long top);

  @StructureSetter("right")
  VMODSrect setRight(long right);

  @StructureSetter("bottom")
  VMODSrect setBottom(long bottom);

}
