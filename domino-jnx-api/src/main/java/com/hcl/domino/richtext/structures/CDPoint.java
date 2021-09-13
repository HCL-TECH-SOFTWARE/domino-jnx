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

import com.hcl.domino.richtext.Point;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

@StructureDefinition(
    name = "CDPOINT",
    members = {
      @StructureMember(name = "x", type = long.class),
      @StructureMember(name = "y", type = long.class)
    }
  )
public interface CDPoint extends MemoryStructure, Point {

  @StructureGetter("x")
  @Override
  long getX();

  @StructureGetter("y")
  @Override
  long getY();

  @StructureSetter("x")
  @Override
  CDPoint setX(long x);

  @StructureSetter("y")
  @Override
  CDPoint setY(long y);
  
}
