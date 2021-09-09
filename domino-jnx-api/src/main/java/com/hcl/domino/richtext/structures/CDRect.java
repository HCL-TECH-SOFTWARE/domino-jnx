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

import com.hcl.domino.richtext.Rectangle;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

@StructureDefinition(
    name = "CDRECT",
    members = {
      @StructureMember(name = "left", type = long.class, unsigned = true),
      @StructureMember(name = "top", type = long.class, unsigned = true),
      @StructureMember(name = "right", type = long.class, unsigned = true),
      @StructureMember(name = "bottom", type = long.class, unsigned = true)
    }
  )
public interface CDRect extends MemoryStructure, Rectangle {

  @StructureGetter("left")
  @Override
  long getLeft();

  @StructureGetter("top")
  @Override
  long getTop();
  
  @StructureGetter("right")
  @Override
  long getRight();

  @StructureGetter("bottom")
  @Override
  long getBottom();

  @StructureSetter("left")
  @Override
  CDRect setLeft(long left);

  @StructureSetter("top")
  @Override
  CDRect setTop(long top);
  
  @StructureSetter("right")
  @Override
  CDRect setRight(long right);

  @StructureSetter("bottom")
  @Override
  CDRect setBottom(long bottom);
  
}
