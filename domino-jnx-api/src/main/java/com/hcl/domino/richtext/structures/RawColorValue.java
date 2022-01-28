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
 * Represents ARGB color values stored as DWORDs.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
@StructureDefinition(
  name = "RAW_COLOR_VALUE", // NB: not a real structure name
  endianSensitive = true,
  members = {
    @StructureMember(name = "Red", type = byte.class, unsigned = true),
    @StructureMember(name = "Green", type = byte.class, unsigned = true),
    @StructureMember(name = "Blue", type = byte.class, unsigned = true),
    @StructureMember(name = "Alpha", type = byte.class, unsigned = true),
  }
)
public interface RawColorValue extends MemoryStructure {
  @StructureGetter("Red")
  short getRed();
  
  @StructureSetter("Red")
  RawColorValue setRed(short red);

  @StructureGetter("Green")
  short getGreen();
  
  @StructureSetter("Green")
  RawColorValue setGreen(short green);
  
  @StructureGetter("Blue")
  short getBlue();
  
  @StructureSetter("Blue")
  RawColorValue setBlue(short blue);
  
  /**
   * Copies the color data values from another {@link RawColorValue}
   * 
   * @param otherColor color
   */
  default void copyFrom(RawColorValue otherColor) {
    setRed(otherColor.getRed());
    setGreen(otherColor.getGreen());
    setBlue(otherColor.getBlue());
  }
}
