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
package com.hcl.domino.richtext.structures;

import java.util.Optional;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "FRAMESETLENGTH",
  members = {
    @StructureMember(name = "Type", type = FrameSizingType.class),
    @StructureMember(name = "Value", type = short.class, unsigned = true)
  }
)
public interface FramesetLength extends MemoryStructure {
  @StructureGetter("Type")
  Optional<FrameSizingType> getType();
  
  /**
   * Retrieves the type value as a raw {@code short}.
   * 
   * @return the type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Type")
  short getTypeRaw();
  
  @StructureSetter("Type")
  FramesetLength setType(FrameSizingType type);
  
  /**
   * Sets the type value as a raw {@code short}.
   * 
   * @param type the type to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Type")
  FramesetLength setTypeRaw(short type);
  
  @StructureGetter("Value")
  int getValue();
  
  @StructureSetter("Value")
  FramesetLength setValue(int value);
}
